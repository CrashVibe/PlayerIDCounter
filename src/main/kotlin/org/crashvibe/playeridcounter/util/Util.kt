package org.crashvibe.playeridcounter.util

import org.bukkit.ChatColor
import org.crashvibe.playeridcounter.PlayerIDCounter
import org.crashvibe.playeridcounter.PlayerIDCounter.Companion.instance
import java.util.UUID

object Util {

    fun getPlayerId(uuid: UUID): Int {
        return PlayerIDCounter.playerIds.getOrDefault(uuid, -1) // 获取玩家ID
    }

    fun getLang(key: String): String {
        return ChatColor.translateAlternateColorCodes('&', instance.langConfig.getString(key, key)!!) // 获取语言配置
    }
}
