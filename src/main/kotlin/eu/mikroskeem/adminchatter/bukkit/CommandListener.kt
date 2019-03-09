/*
 * This file is part of project Adminchatter, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2018-2019 Mark Vainomaa <mikroskeem@mikroskeem.eu>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package eu.mikroskeem.adminchatter.bukkit

import eu.mikroskeem.adminchatter.common.adminchatTogglePlayers
import eu.mikroskeem.adminchatter.common.platform.BukkitPlatformSender
import eu.mikroskeem.adminchatter.common.platform.config
import eu.mikroskeem.adminchatter.common.sendChannelChat
import eu.mikroskeem.adminchatter.common.utils.BASE_CHAT_PERMISSION
import eu.mikroskeem.adminchatter.common.utils.passMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

/**
 * @author Mark Vainomaa
 */
@Deprecated("This solution is ugly as hell, get rid as soon as possible")
class CommandListener: Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun on(event: PlayerCommandPreprocessEvent) {
        val player = BukkitPlatformSender(event.player)

        val splitted = event.message.split(" ", limit = 2)
        val command = splitted[0].substring(1)
        val message = if(splitted.size > 1) splitted.subList(1, splitted.size).joinToString(separator = " ") else ""

        // Check if player wanted to speak in admin chat
        var toggleChat = false
        val channelInfo = config.channels.firstOrNull {
            if(it.channelName == command || it.commandAliases.contains(command)) {
                return@firstOrNull true
            }

            if(it.toggleCommandName == command || it.toggleCommandAliases.contains(command)) {
                toggleChat = true
                return@firstOrNull true
            }

            return@firstOrNull false
        } ?: return

        event.isCancelled = true

        if(!player.hasPermission(BASE_CHAT_PERMISSION + channelInfo.channelName)) {
            // TODO: send "permission denied" message here
            return
        }

        if(toggleChat) {
            // Get player's current channel
            val currentChannel = adminchatTogglePlayers[player.base]

            // If player has no channel
            when {
                currentChannel == null -> {
                    adminchatTogglePlayers[player.base] = channelInfo
                    player.passMessage(config.messages.toggledOn, channelInfo)
                }

                currentChannel.channelName == channelInfo.channelName -> {
                    // If toggled channel equals to one representing a command, untoggle
                    adminchatTogglePlayers.remove(player.base)
                    player.passMessage(config.messages.toggledOff, channelInfo)
                }

                else -> {
                    // If toggled channel is not same as one representing a command, switch channel
                    adminchatTogglePlayers[player.base] = channelInfo
                    player.passMessage(config.messages.channelSwitched, channelInfo)
                }
            }
        } else {
            if(message.isEmpty()) {
                player.passMessage(config.messages.mustSupplyAMessage)
                return
            }

            player.sendChannelChat(channelInfo, message)
        }
    }
}