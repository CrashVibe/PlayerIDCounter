package org.crashvibe.playeridcounter.hooks

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.crashvibe.playeridcounter.PlayerIDCounter
import org.crashvibe.playeridcounter.util.Util

class IDPlaceholderExpansion(private val plugin: PlayerIDCounter) : PlaceholderExpansion() {

    override fun persist(): Boolean {
        return true
    }

    override fun canRegister(): Boolean {
        return true
    }

    override fun getIdentifier(): String {
        return "playerid" // Placeholder标识
    }

    override fun getAuthor(): String {
        return plugin.getDescription().getAuthors().toString()
    }

    override fun getVersion(): String {
        return plugin.getDescription().getVersion()
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String {
        if (player == null) {
            return "None"
        }

        if (params.equals("id", ignoreCase = true)) {
            return Util.getPlayerId(player.uniqueId).toString() // 返回玩家ID
        }

        if (params.matches("\\d".toRegex())) {
            val id = params.toInt()
            val name: String? = PlayerIDCounter.playerIds
                .entries
                .firstOrNull { it.value == id }
                ?.key
                ?.let(Bukkit::getOfflinePlayer)
                ?.name

            return name ?: "None" // 返回玩家名
        }

        return "None"
    }
}
