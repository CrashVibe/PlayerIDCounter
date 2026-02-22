package org.crashvibe.playeridcounter.util

import org.crashvibe.playeridcounter.PlayerIDCounter
import java.util.UUID

fun getPlayerId(uuid: UUID): Int {
    return PlayerIDCounter.playerIds.getOrDefault(uuid, -1) // 获取玩家ID
}
