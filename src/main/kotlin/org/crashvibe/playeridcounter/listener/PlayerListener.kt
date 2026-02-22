package org.crashvibe.playeridcounter.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.crashvibe.playeridcounter.PlayerIDCounter
import org.crashvibe.playeridcounter.config.Config
import org.crashvibe.playeridcounter.config.Config.getLang
import org.crashvibe.playeridcounter.util.getPlayerId

class PlayerListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.getPlayer()
        val uuid = player.uniqueId

        PlayerIDCounter.foliaLib.scheduler.runAsync {
            var id = getPlayerId(uuid)
            if (id == -1) {
                id = PlayerIDCounter.nextId.get()
                PlayerIDCounter.playerIds[uuid] = id

                Config.playersConfig.set(uuid.toString(), id)
                Config.playersConfig.set("next-id", PlayerIDCounter.nextId.incrementAndGet()) // 分配新ID

                Config.savePlayerData()
            }

            val welcomeMessage = Config.langData.welcomeMessage.replace("{id}", id.toString())

            player.sendMessage(getLang(welcomeMessage)) // 发送欢迎消息
        }
    }
}
