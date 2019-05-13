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

package eu.mikroskeem.adminchatter.bungee.listeners

import com.google.common.eventbus.Subscribe
import eu.mikroskeem.adminchatter.bungee.plugin
import eu.mikroskeem.adminchatter.bungee.proxy
import eu.mikroskeem.adminchatter.common.config.ChannelCommandInfo
import eu.mikroskeem.adminchatter.common.events.ChannelToggleEvent
import eu.mikroskeem.adminchatter.common.platform.config
import eu.mikroskeem.adminchatter.common.utils.colored
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.util.UUID
import java.util.WeakHashMap
import java.util.concurrent.TimeUnit

/**
 * @author Mark Vainomaa
 */
class ChannelListener {
    private val channelNotifyTasks = WeakHashMap<ProxiedPlayer, ChannelNotifierTask>()

    @Subscribe
    fun on(event: ChannelToggleEvent) {
        val player = event.sender.base as? ProxiedPlayer ?: return

        channelNotifyTasks.remove(player)?.cancel()
        if(event.toChannel != null) {
            channelNotifyTasks[player] = ChannelNotifierTask(player, event.toChannel!!)
        }
    }

    class ChannelNotifierTask(player: ProxiedPlayer, private val channel: ChannelCommandInfo): Runnable {
        private val playerUuid: UUID
        internal val task: ScheduledTask

        init {
            playerUuid = player.uniqueId
            task = proxy.scheduler.schedule(plugin, this, 0, 2, TimeUnit.SECONDS)
        }

        fun cancel() = task.cancel()

        override fun run() {
            val player = proxy.getPlayer(playerUuid) ?: run {
                cancel()
                return
            }

            val message = (config.messages.currentlyInAChannel.takeIf { it.isNotEmpty() } ?: return).colored()
                    .replace("{channel_name}", channel.channelName)
                    .replace("{pretty_channel_name}", channel.prettyChannelName.colored())

            player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent(message))
        }
    }
}