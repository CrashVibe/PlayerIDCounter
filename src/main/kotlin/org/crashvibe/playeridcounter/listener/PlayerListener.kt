package org.crashvibe.playeridcounter.listener

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.crashvibe.playeridcounter.PlayerIDCounter
import org.crashvibe.playeridcounter.util.Util
import org.crashvibe.playeridcounter.util.Util.getLang

class PlayerListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val instance = PlayerIDCounter.instance
        val player = event.getPlayer()
        val uuid = player.uniqueId

        Bukkit.getScheduler().runTaskAsynchronously(instance, Runnable {
            var id = Util.getPlayerId(uuid)
            if (id == -1) {
                id = PlayerIDCounter.nextId.get()
                PlayerIDCounter.playerIds[uuid] = id

                instance.playersConfig.set(uuid.toString(), id)
                instance.playersConfig.set("next-id", PlayerIDCounter.nextId.incrementAndGet()) // 分配新ID

                instance.savePlayerData()
            }

            val welcomeMessage = getLang("welcome_message").replace("{id}", id.toString())

            player.sendMessage(welcomeMessage) // 发送欢迎消息
        })
    }
}
