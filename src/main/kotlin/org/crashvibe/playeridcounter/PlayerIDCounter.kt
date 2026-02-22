package org.crashvibe.playeridcounter

import com.tcoded.folialib.FoliaLib
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.crashvibe.playeridcounter.config.Config
import org.crashvibe.playeridcounter.config.Config.getLang
import org.crashvibe.playeridcounter.hooks.IDPlaceholderExpansion
import org.crashvibe.playeridcounter.listener.PlayerListener
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class PlayerIDCounter : JavaPlugin() {

    override fun onEnable() {
        instance = this
        foliaLib = FoliaLib(instance)

        Config.init(dataPath) // 初始化文件

        playerIds = ConcurrentHashMap<UUID, Int>()
        nextId = AtomicInteger(Config.playersConfig.getInt("next-id", 1))

        Config.playersConfig.getKeys(false).forEach(Consumer { key: String ->
            if (key != "next-id") {
                val uuid = UUID.fromString(key)
                val id = Config.playersConfig.getInt(key)
                playerIds[uuid] = id // 加载玩家ID
            }
        })

        Bukkit.getPluginManager().registerEvents(PlayerListener(), instance)

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            IDPlaceholderExpansion(instance).register() // 注册PlaceholderAPI
        } else {
            LOGGER.warn(getLang(Config.langData.papiNotFound))
        }

        LOGGER.info(getLang(Config.langData.pluginEnabled)) // 插件启用
    }

    override fun onDisable() {
        Config.savePlayerData() // 保存玩家数据
        LOGGER.info(getLang(Config.langData.pluginDisabled)) // 插件禁用
    }

    companion object {
        val LOGGER: ComponentLogger = ComponentLogger.logger(PlayerIDCounter::class.java.simpleName)

        lateinit var instance: PlayerIDCounter
        lateinit var foliaLib: FoliaLib

        lateinit var playerIds: ConcurrentHashMap<UUID, Int>
        lateinit var nextId: AtomicInteger
    }
}
