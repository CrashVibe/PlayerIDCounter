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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerIDCounter extends JavaPlugin implements Listener {

    // 文件对象，用于存储玩家数据和语言文件
    private File playersFile;
    private FileConfiguration playersConfig;
    private File langFile;
    private FileConfiguration langConfig;

    // 存储玩家ID的映射表，UUID对应玩家ID
    private HashMap<UUID, Integer> playerIds;
    // 下一个可用的ID
    private int nextId;

    @Override
    public void onEnable() {
        // 初始化玩家数据和语言文件
        initFiles();

        this.playerIds = new HashMap<>();
        // 从配置文件读取下一个ID，如果没有则默认设置为1
        this.nextId = playersConfig.getInt("next-id", 1);

        // 加载现有玩家 ID
        for (String key : playersConfig.getKeys(false)) {
            if (key.equals("next-id")) continue; // 跳过 "next-id" 配置项
            UUID uuid = UUID.fromString(key);
            int id = playersConfig.getInt(key);
            playerIds.put(uuid, id);
        }

        // 注册事件监听器，使插件能够响应玩家加入事件
        Bukkit.getPluginManager().registerEvents(this, this);

        // 如果 PlaceholderAPI 插件可用，注册扩展
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new IDPlaceholderExpansion(this).register();
        } else {
            getLogger().warning(getLang("papi_not_found"));
        }

        getLogger().info(getLang("plugin_enabled"));
    }

    @Override
    public void onDisable() {
        // 在插件禁用时保存玩家数据
        savePlayerData();
        getLogger().info(getLang("plugin_disabled"));
    }

    // 玩家加入事件处理器
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        // 异步处理玩家ID分配
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            // 如果玩家没有ID，则为其分配一个新的ID
            if (!playerIds.containsKey(playerUuid)) {
                synchronized (this) { // 使用同步锁避免并发冲突
                    if (!playerIds.containsKey(playerUuid)) {
                        playerIds.put(playerUuid, nextId);
                        playersConfig.set(playerUuid.toString(), nextId);
                        nextId++; // 增加ID值
                        playersConfig.set("next-id", nextId);
                        savePlayerData(); // 保存更新后的玩家数据
                    }
                }
            }

            int playerId = playerIds.get(playerUuid);

            // 在主线程中发送欢迎消息
            Bukkit.getScheduler().runTask(this, () -> {
                player.sendMessage(ChatColor.GREEN + getLang("welcome_message").replace("{id}", String.valueOf(playerId)));
            });
        });
    }

    // 获取玩家ID，根据UUID查询，如果没有找到则返回-1
    public int getPlayerId(UUID uuid) {
        return playerIds.getOrDefault(uuid, -1);
    }

    // 从语言配置文件中获取翻译文本
    public String getLang(String key) {
        return ChatColor.translateAlternateColorCodes('&', langConfig.getString(key, key));
    }

    // 初始化所有必要的文件，包括 config.yml, players.yml, lang.yml
    private void initFiles() {
        // 初始化 config.yml
        saveDefaultConfig();

        // 初始化 players.yml
        playersFile = new File(getDataFolder(), "players.yml");
        if (!playersFile.exists()) {
            saveResource("players.yml", false);
        }
        playersConfig = YamlConfiguration.loadConfiguration(playersFile);

        // 初始化 lang.yml
        langFile = new File(getDataFolder(), "lang.yml");
        if (!langFile.exists()) {
            saveResource("lang.yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    // 保存玩家数据到 players.yml 文件
    private void savePlayerData() {
        try {
            playersConfig.save(playersFile);
        } catch (IOException e) {
            getLogger().severe("无法保存玩家数据到 players.yml！");
            e.printStackTrace();
        }
    }

    // PlaceholderAPI 扩展，用于显示玩家ID
    public static class IDPlaceholderExpansion extends PlaceholderExpansion {

        private final PlayerIDCounter plugin;

        // 构造方法，传入插件实例
        public IDPlaceholderExpansion(PlayerIDCounter plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean persist() {
            return true; // 该扩展会在重载后继续存在
        }

        @Override
        public boolean canRegister() {
            return true;
        }

        @Override
        public String getIdentifier() {
            return "playerid"; // 占位符标识符
        }

        @Override
        public String getAuthor() {
            return plugin.getDescription().getAuthors().toString();
        }

        @Override
        public String getVersion() {
            return plugin.getDescription().getVersion();
        }

        // 处理占位符请求
        @Override
        public String onPlaceholderRequest(Player player, String params) {
            if (player == null) {
                return "None";
            }

            if (params.equalsIgnoreCase("id")) {
                return String.valueOf(plugin.getPlayerId(player.getUniqueId()));
            }

            return null;
        }
    }
}
