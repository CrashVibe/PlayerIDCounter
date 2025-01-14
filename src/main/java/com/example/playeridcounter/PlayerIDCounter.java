package com.example.playeridcounter;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerIDCounter extends JavaPlugin implements Listener {

    // 单例实例，方便外部访问插件方法
    private static PlayerIDCounter instance;

    // 玩家数据文件和语言文件配置对象
    private File playersFile;
    private FileConfiguration playersConfig;
    private FileConfiguration langConfig;

    // 映射表
    private HashMap<UUID, Integer> playerIds;

    // 可分配ID
    private int nextId;

    // 根据UUID获取玩家ID
    public static int getPlayerId(UUID uuid) {
        return instance.playerIds.getOrDefault(uuid, -1); // 如果未找到返回-1
    }

    // 从语言配置文件中获取翻译文本
    public static String getLang(String key) {
        return ChatColor.translateAlternateColorCodes('&', instance.langConfig.getString(key, key));
    }

    @Override
    public void onEnable() {
        // 设置单例实例
        instance = this;

        initFiles();

        // 初始化玩家ID映射表和下一个ID
        this.playerIds = new HashMap<>();
        this.nextId = playersConfig.getInt("next-id", 1); // 从配置文件读取下一个ID，默认值为1

        // 加载已有玩家的ID数据
        for (String key : playersConfig.getKeys(false)) {
            if (key.equals("next-id")) continue; // 跳过 "next-id" 配置项
            UUID uuid = UUID.fromString(key);
            int id = playersConfig.getInt(key);
            playerIds.put(uuid, id);
        }

        // 注册
        Bukkit.getPluginManager().registerEvents(this, this);
        // PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new IDPlaceholderExpansion(this).register();
        } else {
            getLogger().warning(getLang("papi_not_found")); // 提示 PlaceholderAPI 未找到
        }

        // 插件启动完成提示
        getLogger().info(getLang("plugin_enabled"));
    }

    @Override
    public void onDisable() {
        // 插件关闭时保存玩家数据
        savePlayerData();
        getLogger().info(getLang("plugin_disabled"));
    }

    // 玩家加入服务器事件处理
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        // 异步处理玩家ID分配
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            // 如果玩家没有ID，则分配一个新ID
            if (!playerIds.containsKey(playerUuid)) {
                synchronized (this) { // 加锁防止并发问题
                    if (!playerIds.containsKey(playerUuid)) {
                        playerIds.put(playerUuid, nextId);
                        playersConfig.set(playerUuid.toString(), nextId);
                        nextId++;
                        playersConfig.set("next-id", nextId);
                        savePlayerData();
                    }
                }
            }

            int playerId = playerIds.get(playerUuid);

            // 在主线程中发送欢迎消息
            Bukkit.getScheduler().runTask(this, () -> {
                String welcomeMessage = getLang("welcome_message").replace("{id}", String.valueOf(playerId));
                player.sendMessage(ChatColor.GREEN + welcomeMessage);
            });
        });
    }

    // 初始化所有配置文件
    private void initFiles() {
        // 初始化 config.yml
        saveDefaultConfig();

        // 初始化 players.yml
        playersFile = new File(getDataFolder(), "players.yml");
        if (!playersFile.exists()) {
            saveResource("players.yml", false); // 如果文件不存在，从插件JAR中复制默认文件
        }
        playersConfig = YamlConfiguration.loadConfiguration(playersFile);

        // 初始化 lang.yml
        File langFile = new File(getDataFolder(), "lang.yml");
        if (!langFile.exists()) {
            saveResource("lang.yml", false); // 如果文件不存在，从插件JAR中复制默认文件
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    // 保存玩家数据到 players.yml 文件
    private void savePlayerData() {
        try {
            playersConfig.save(playersFile); // 保存配置文件
        } catch (IOException e) {
            getLogger().severe("无法保存玩家数据到 players.yml！");
            getLogger().severe("Error saving player data: " + e.getMessage());
        }
    }

    // PlaceholderAPI 扩展类，用于提供玩家ID占位符
    public static class IDPlaceholderExpansion extends PlaceholderExpansion {

        private final PlayerIDCounter plugin;

        public IDPlaceholderExpansion(PlayerIDCounter plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean persist() {
            return true; // 占位符扩展在重载后继续存在
        }

        @Override
        public boolean canRegister() {
            return true; // 允许注册
        }

        @Override
        public @NotNull String getIdentifier() {
            return "playerid"; // 占位符标识符
        }

        @Override
        public @NotNull String getAuthor() {
            return plugin.getDescription().getAuthors().toString(); // 插件作者
        }

        @Override
        public @NotNull String getVersion() {
            return plugin.getDescription().getVersion(); // 插件版本
        }

        // 占位符请求
        @Override
        public String onPlaceholderRequest(Player player, @NotNull String params) {
            if (player == null) {
                return "";
            }

            // 如果占位符参数是 "id"，返回玩家的ID
            if (params.equalsIgnoreCase("id")) {
                return String.valueOf(PlayerIDCounter.getPlayerId(player.getUniqueId()));
            }

            return null; // 都他妈不是就返回 null
        }
    }
}