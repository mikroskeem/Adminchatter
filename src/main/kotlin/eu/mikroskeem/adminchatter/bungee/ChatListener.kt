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

package eu.mikroskeem.adminchatter.bungee

import eu.mikroskeem.adminchatter.common.config.ChannelCommandInfo
import eu.mikroskeem.adminchatter.common.utils.BASE_CHAT_PERMISSION
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

/**
 * @author Mark Vainomaa
 */
class ChatListener: Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: ChatEvent) {
        var message = event.message
        val player = event.sender as? ProxiedPlayer ?: return

        // Cancelled events and commands aren't useful here
        if(event.isCancelled || event.isCommand)
            return

        // Figure out what channel is player in and check if player has channel toggle
        var wasToggle = false
        val channel: ChannelCommandInfo = if(adminchatTogglePlayers[player] != null) {
            wasToggle = true
            adminchatTogglePlayers[player]!!
        } else {
            // Find channel by prefix what player is using, or return
            plugin.channelsByChatPrefix.filterKeys { message.startsWith(it) }
                    .takeIf { it.isNotEmpty() }
                    ?.values?.firstOrNull()
                    ?: return
        }

        // Check if player has permission for given channel
        if(!player.hasPermission(BASE_CHAT_PERMISSION + channel.channelName))
            return

        // If player didn't have toggled the channel
        if(!wasToggle) {
            if(message != channel.messagePrefix && message.startsWith(channel.messagePrefix)) {
                // Strip prefix
                message = message.substring(channel.messagePrefix.length)
            } else {
                // Nothing to do here
                return
            }
        }

        // Cancel event as message shouldn't reach to backend server
        event.isCancelled = true

        // Send message to channel
        player.sendChannelChat(channel, message)
    }
}