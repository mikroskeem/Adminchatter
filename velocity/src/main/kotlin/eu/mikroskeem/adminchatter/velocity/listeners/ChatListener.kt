/*
 * This file is part of project Adminchatter, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2018-2022 Mark Vainomaa <mikroskeem@mikroskeem.eu>
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

package eu.mikroskeem.adminchatter.velocity.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.proxy.ServerConnection
import eu.mikroskeem.adminchatter.common.channelsByName
import eu.mikroskeem.adminchatter.common.handleToggleChat
import eu.mikroskeem.adminchatter.common.platform.ProxiedPlatformSender
import eu.mikroskeem.adminchatter.common.sendChannelChat
import eu.mikroskeem.adminchatter.common.utils.PLUGIN_CHANNEL_PROXY
import eu.mikroskeem.adminchatter.common.utils.deserializeProxyChat
import eu.mikroskeem.adminchatter.velocity.VelocityChatEvent
import eu.mikroskeem.adminchatter.velocity.VelocityPlatformSender
import eu.mikroskeem.adminchatter.velocity.plugin

/**
 * @author Mark Vainomaa
 */
class ChatListener {
    @Subscribe
    fun on(event: PlayerChatEvent) {
        val player = VelocityPlatformSender(event.player)

        // Cancelled events aren't useful here
        if(!event.result.isAllowed)
            return

        handleToggleChat(VelocityChatEvent(event), player, event.message)
    }

    @Subscribe
    fun on(event: PluginMessageEvent) {
        val serverConnection = event.source as? ServerConnection ?: return
        if (event.identifier.id != PLUGIN_CHANNEL_PROXY) {
            return
        }

        val (channelName, sender, isConsole, message) = deserializeProxyChat(event.data)
        val channel = channelsByName[channelName] ?: run {
            plugin.logger.warn("Received proxied chat for unknown channel '{}'", channelName)
            return
        }
        ProxiedPlatformSender(sender, isConsole, serverConnection.serverInfo.name).sendChannelChat(channel, message)
    }
}