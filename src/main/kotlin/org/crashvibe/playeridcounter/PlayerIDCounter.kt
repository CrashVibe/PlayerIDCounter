package org.crashvibe.playeridcounter

import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.crashvibe.playeridcounter.hooks.IDPlaceholderExpansion
import org.crashvibe.playeridcounter.listener.PlayerListener
import org.crashvibe.playeridcounter.util.Util.getLang
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class PlayerIDCounter : JavaPlugin(), Listener {

    lateinit var playersConfigFile: File
    lateinit var playersConfig: FileConfiguration
    lateinit var langConfig: FileConfiguration

    override fun onEnable() {
        instance = this

        initFiles() // 初始化文件

        playerIds = ConcurrentHashMap<UUID, Int>()
        nextId = AtomicInteger(playersConfig.getInt("next-id", 1))

        playersConfig.getKeys(false).forEach(Consumer { key: String? ->
            if (key != "next-id") {
                val uuid = UUID.fromString(key)
                val id = playersConfig.getInt(key!!)
                playerIds[uuid] = id // 加载玩家ID
            }
        })

        Bukkit.getPluginManager().registerEvents(PlayerListener(), instance)

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            IDPlaceholderExpansion(instance).register() // 注册PlaceholderAPI
        } else {
            logger.warning(getLang("papi_not_found"))
        }

        logger.info(getLang("plugin_enabled")) // 插件启用
    }

    override fun onDisable() {
        savePlayerData() // 保存玩家数据
        logger.info(getLang("plugin_disabled")) // 插件禁用
    }

    private fun initFiles() {
        saveDefaultConfig()

        playersConfigFile = File(dataFolder, "players.yml")
        if (!playersConfigFile.exists()) {
            saveResource("players.yml", false) // 初始化players.yml
        }
        playersConfig = YamlConfiguration.loadConfiguration(playersConfigFile)

        val langFile = File(dataFolder, "lang.yml")
        if (!langFile.exists()) {
            saveResource("lang.yml", false) // 初始化lang.yml
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile)
    }

    fun savePlayerData() {
        try {
            playersConfig.save(playersConfigFile) // 保存玩家数据
        } catch (e: IOException) {
            logger.severe("无法保存玩家数据到 players.yml！错误: " + e.message)
        }
    }

    companion object {
        lateinit var instance: PlayerIDCounter

        lateinit var playerIds: ConcurrentHashMap<UUID, Int>
        lateinit var nextId: AtomicInteger
    }
}
