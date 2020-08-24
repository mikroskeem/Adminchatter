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

package eu.mikroskeem.adminchatter.velocity

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import eu.mikroskeem.adminchatter.common.config.AdminchatterConfig
import eu.mikroskeem.adminchatter.common.platform.Platform
import eu.mikroskeem.adminchatter.common.platform.PlatformEvent
import eu.mikroskeem.adminchatter.common.platform.PlatformSender
import eu.mikroskeem.adminchatter.common.utils.PLUGIN_CHANNEL_SOUND
import net.kyori.adventure.text.Component

/**
 * @author Mark Vainomaa
 */
private val channelIdentifier = run {
    val (namespace, channel) = PLUGIN_CHANNEL_SOUND.split(":", limit = 2)
    MinecraftChannelIdentifier.create(namespace, channel)
}

class VelocityPlatform(private val plugin: AdminchatterPlugin): Platform {
    override val onlinePlayers: Collection<PlatformSender> get() = plugin.server.allPlayers.map { VelocityPlatformSender(it) }
    override val isProxy: Boolean get() = true
    override val consoleSender: PlatformSender get() = VelocityPlatformSender(plugin.server.consoleCommandSource)
    override val config: AdminchatterConfig get() = plugin.configLoader.configuration
}

class VelocityPlatformSender(val sender: CommandSource): PlatformSender {
    override val base: Any get() = sender
    override val name: String get() = (sender as? Player)?.username ?: (if(isConsole) "CONSOLE" else "")
    override fun sendMessage(component: Component) = sender.sendMessage(component)
    override fun hasPermission(node: String): Boolean = sender.hasPermission(node)
    override val isConsole: Boolean get() = sender === plugin.server.consoleCommandSource
    override val serverName: String get() = (sender as? Player)?.currentServer?.orElse(null)?.serverInfo?.name ?: ""
    override fun playSound(soundData: ByteArray) { (sender as? Player)?.currentServer?.orElse(null)?.sendPluginMessage(channelIdentifier, soundData) }
}

class VelocityChatEvent(private val event: PlayerChatEvent): PlatformEvent {
    override var isCancelled: Boolean
        get() = event.result.isAllowed
        set(value) { event.result = if(value) PlayerChatEvent.ChatResult.denied() else PlayerChatEvent.ChatResult.allowed() }
}