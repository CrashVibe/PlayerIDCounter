package org.crashvibe.playeridcounter.config

import de.exlll.configlib.Configuration

/**
 * 插件配置数据结构
 */
@Configuration
data class PluginConfig(
    var settings: Settings = Settings(),
) {
    @Configuration
    data class Settings(
        var debug: Boolean = false,
    )
}
