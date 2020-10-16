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

package eu.mikroskeem.adminchatter.bungee

import eu.mikroskeem.adminchatter.common.config.AdminchatterConfig
import eu.mikroskeem.adminchatter.common.platform.Platform
import eu.mikroskeem.adminchatter.common.platform.PlatformEvent
import eu.mikroskeem.adminchatter.common.platform.PlatformSender
import eu.mikroskeem.adminchatter.common.utils.PLUGIN_CHANNEL_SOUND
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Cancellable

/**
 * @author Mark Vainomaa
 */
class BungeePlatform(private val plugin: AdminchatterPlugin): Platform {
    override val onlinePlayers: Collection<PlatformSender> get() = plugin.proxy.players.map { BungeePlatformSender(it) }
    override val isProxy: Boolean get() = true
    override val consoleSender: PlatformSender get() = BungeePlatformSender(plugin.proxy.console)
    override val config: AdminchatterConfig get() = plugin.configLoader.configuration
}

class BungeePlatformSender(val sender: CommandSender): PlatformSender {
    override val base: Any get() = sender
    override val name: String get() = sender.name
    override fun sendMessage(component: Component) = audiences.sender(sender).sendMessage(Identity.nil(), component)
    override fun hasPermission(node: String): Boolean = sender.hasPermission(node)
    override val isConsole: Boolean get() = sender === ProxyServer.getInstance().console
    override val serverName: String get() = (sender as? ProxiedPlayer)?.server?.info?.name ?: ""
    override fun playSound(soundData: ByteArray) { (sender as? ProxiedPlayer)?.server?.sendData(PLUGIN_CHANNEL_SOUND, soundData) }

    companion object {
        val audiences = BungeeAudiences.create(plugin)
    }
}

class BungeeEvent(private val event: Cancellable): PlatformEvent {
    override var isCancelled: Boolean
        get() = event.isCancelled
        set(value) { event.isCancelled = value }
}