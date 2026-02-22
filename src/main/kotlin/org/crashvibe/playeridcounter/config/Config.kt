package org.crashvibe.playeridcounter.config

import de.exlll.configlib.YamlConfigurations
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.crashvibe.playeridcounter.PlayerIDCounter
import org.crashvibe.playeridcounter.PlayerIDCounter.Companion.instance
import java.io.File
import java.io.IOException
import java.nio.file.Path

object Config {

    lateinit var configData: PluginConfig
        private set
    lateinit var langData: Lang
        private set
    lateinit var playersConfigFile: File
    lateinit var playersConfig: FileConfiguration

    fun init(configFile: Path) {
        configData = YamlConfigurations.update(
            configFile.resolve("config.yml"), // 初始化 config.yml
            PluginConfig::class.java,
        )
        langData = YamlConfigurations.update(
            configFile.resolve("lang.yml"), // 初始化 lang.yml
            Lang::class.java,
        )

        instance.saveDefaultConfig()

        playersConfigFile = File(instance.dataFolder, "players.yml")
        if (!playersConfigFile.exists()) {
            instance.saveResource("players.yml", false) // 初始化 players.yml
        }
        playersConfig = YamlConfiguration.loadConfiguration(playersConfigFile)
    }

    fun savePlayerData() {
        try {
            playersConfig.save(playersConfigFile) // 保存玩家数据
        } catch (e: IOException) {
            PlayerIDCounter.LOGGER.error(text("无法保存玩家数据到 players.yml！错误: "), e)
        }
    }

    fun getLang(key: String): Component {
        return MiniMessage.miniMessage().deserialize(key) // 获取语言配置
    }
}
