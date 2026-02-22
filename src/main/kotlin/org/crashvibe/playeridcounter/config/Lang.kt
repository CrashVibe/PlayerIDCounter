package org.crashvibe.playeridcounter.config

import de.exlll.configlib.Configuration

@Configuration
data class Lang(
    var pluginEnabled: String = "<green>插件已启用！",
    var pluginDisabled: String = "<red>插件已禁用！",
    var papiNotFound: String = "<red>PlaceholderAPI 未找到，功能可能无法使用！",
    var welcomeMessage: String = "<yellow>欢迎！你的ID是：{id}"
)
