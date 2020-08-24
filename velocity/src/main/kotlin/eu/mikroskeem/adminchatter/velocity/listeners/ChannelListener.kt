/*
 * This file is part of project Adminchatter, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2018-2020 Mark Vainomaa <mikroskeem@mikroskeem.eu>
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

import com.google.common.eventbus.Subscribe
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.scheduler.ScheduledTask
import eu.mikroskeem.adminchatter.common.channelsByName
import eu.mikroskeem.adminchatter.common.events.ChannelToggleEvent
import eu.mikroskeem.adminchatter.common.platform.config
import eu.mikroskeem.adminchatter.velocity.plugin
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.util.UUID
import java.util.WeakHashMap
import java.util.concurrent.TimeUnit

/**
 * @author Mark Vainomaa
 */
class ChannelListener {
    private val channelNotifyTasks = WeakHashMap<Player, ChannelNotifierTask>()

    @Subscribe
    fun on(event: ChannelToggleEvent) {
        val player = event.sender.base as? Player ?: return

        channelNotifyTasks.remove(player)?.cancel()
        if(event.toChannel != null) {
            channelNotifyTasks[player] = ChannelNotifierTask(player, event.toChannel!!.channelName)
        }
    }

    class ChannelNotifierTask(player: Player, private val channelName: String): Runnable {
        private val playerUuid: UUID
        internal val task: ScheduledTask

        init {
            playerUuid = player.uniqueId
            task = plugin.server.scheduler.buildTask(plugin, this)
                    .repeat( 2, TimeUnit.SECONDS)
                    .schedule()
        }

        fun cancel() = task.cancel()

        override fun run() {
            val player = plugin.server.getPlayer(playerUuid).orElse(null) ?: run {
                cancel()
                return
            }

            val channel = channelsByName[channelName] ?: run {
                cancel()
                return
            }

            val message = (config.messages.currentlyInAChannel.takeIf { it.isNotEmpty() } ?: return)
                    .replace("{channel_name}", channel.prettyChannelName)

            player.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(message))
        }
    }
}