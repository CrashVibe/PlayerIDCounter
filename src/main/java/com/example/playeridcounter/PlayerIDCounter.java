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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerIDCounter extends JavaPlugin implements Listener {

    private static PlayerIDCounter instance;

    private File playersFile;
    private FileConfiguration playersConfig;
    private FileConfiguration langConfig;

    private ConcurrentHashMap<UUID, Integer> playerIds;

    private int nextId;

    public static int getPlayerId(UUID uuid) {
        return instance.playerIds.getOrDefault(uuid, -1); // 获取玩家ID
    }

    public static String getLang(String key) {
        return ChatColor.translateAlternateColorCodes('&', instance.langConfig.getString(key, key)); // 获取语言配置
    }

    @Override
    public void onEnable() {
        instance = this;

        initFiles(); // 初始化文件

        this.playerIds = new ConcurrentHashMap<>();
        this.nextId = playersConfig.getInt("next-id", 1);

        playersConfig.getKeys(false).forEach(key -> {
            if (!key.equals("next-id")) {
                UUID uuid = UUID.fromString(key);
                int id = playersConfig.getInt(key);
                playerIds.put(uuid, id); // 加载玩家ID
            }
        });

        Bukkit.getPluginManager().registerEvents(this, this);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new IDPlaceholderExpansion(this).register(); // 注册PlaceholderAPI
        } else {
            getLogger().warning(getLang("papi_not_found"));
        }

        getLogger().info(getLang("plugin_enabled")); // 插件启用
    }

    @Override
    public void onDisable() {
        savePlayerData(); // 保存玩家数据
        getLogger().info(getLang("plugin_disabled")); // 插件禁用
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            if (!playerIds.containsKey(playerUuid)) {
                playerIds.put(playerUuid, nextId);
                playersConfig.set(playerUuid.toString(), nextId);
                nextId++;
                playersConfig.set("next-id", nextId); // 分配新ID
                savePlayerData();
            }

            int playerId = playerIds.get(playerUuid);

            Bukkit.getScheduler().runTask(this, () -> {
                String welcomeMessage = getLang("welcome_message").replace("{id}", String.valueOf(playerId));
                player.sendMessage(ChatColor.GREEN + welcomeMessage); // 发送欢迎消息
            });
        });
    }

    private void initFiles() {
        saveDefaultConfig();

        playersFile = new File(getDataFolder(), "players.yml");
        if (!playersFile.exists()) {
            saveResource("players.yml", false); // 初始化players.yml
        }
        playersConfig = YamlConfiguration.loadConfiguration(playersFile);

        File langFile = new File(getDataFolder(), "lang.yml");
        if (!langFile.exists()) {
            saveResource("lang.yml", false); // 初始化lang.yml
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    private void savePlayerData() {
        try {
            playersConfig.save(playersFile); // 保存玩家数据
        } catch (IOException e) {
            getLogger().severe("无法保存玩家数据到 players.yml！");
            getLogger().severe("Error saving player data: " + e.getMessage());
        }
    }

    public static class IDPlaceholderExpansion extends PlaceholderExpansion {

        private final PlayerIDCounter plugin;

        public IDPlaceholderExpansion(PlayerIDCounter plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public boolean canRegister() {
            return true;
        }

        @Override
        public @NotNull String getIdentifier() {
            return "playerid"; // Placeholder标识
        }

        @Override
        public @NotNull String getAuthor() {
            return plugin.getDescription().getAuthors().toString();
        }

        @Override
        public @NotNull String getVersion() {
            return plugin.getDescription().getVersion();
        }

        @Override
        public String onPlaceholderRequest(Player player, @NotNull String params) {
            if (player == null) {
                return "None";
            }

            if (params.equalsIgnoreCase("id")) {
                return String.valueOf(PlayerIDCounter.getPlayerId(player.getUniqueId())); // 返回玩家ID
            }

            if (params.matches("\\d")) {
                int id = Integer.parseInt(params);
                UUID uuid = plugin.playerIds.entrySet().stream()
                        .filter(entry -> entry.getValue() == id)
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);
                if (uuid != null) {
                    return Bukkit.getOfflinePlayer(uuid).getName(); // 返回玩家名
                } else {
                    return "None"; // 玩家不存在
                }
            }

            return null;
        }
    }
}