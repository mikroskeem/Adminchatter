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

package eu.mikroskeem.adminchatter.common.platform

import eu.mikroskeem.adminchatter.common.config.AdminchatterConfig
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import kotlin.properties.Delegates

/**
 * @author Mark Vainomaa
 */
var currentPlatform: Platform by Delegates.notNull()
    internal set
val config: AdminchatterConfig get() = currentPlatform.config

interface Platform {
    val onlinePlayers: Collection<PlatformSender>
    val isBungee: Boolean
    val consoleSender: PlatformSender
    val config: AdminchatterConfig
}

interface PlatformSender {
    val base: Any
    val name: String
    fun sendMessage(vararg components: BaseComponent)
    fun hasPermission(node: String): Boolean
    val isConsole: Boolean
}

interface PlatformEvent {
    var isCancelled: Boolean
}

inline class BungeePlatform(private val plugin: eu.mikroskeem.adminchatter.bungee.AdminchatterPlugin): Platform {
    override val onlinePlayers: Collection<PlatformSender> get() = plugin.proxy.players.map { BungeePlatformSender(it) }
    override val isBungee: Boolean get() = true
    override val consoleSender: PlatformSender get() = BungeePlatformSender(plugin.proxy.console)
    override val config: AdminchatterConfig get() = plugin.configLoader.configuration
}

inline class BukkitPlatform(private val plugin: eu.mikroskeem.adminchatter.bukkit.AdminchatterPlugin): Platform {
    override val onlinePlayers: Collection<PlatformSender> get() = plugin.server.onlinePlayers.map { BukkitPlatformSender(it) }
    override val isBungee: Boolean get() = false
    override val consoleSender: PlatformSender get() = BukkitPlatformSender(plugin.server.consoleSender)
    override val config: AdminchatterConfig get() = plugin.configLoader.configuration
}

inline class BungeePlatformSender(val sender: net.md_5.bungee.api.CommandSender): PlatformSender {
    override val base: Any get() = sender
    override val name: String get() = sender.name
    override fun sendMessage(vararg components: BaseComponent) = sender.sendMessage(*components)
    override fun hasPermission(node: String): Boolean = sender.hasPermission(node)
    override val isConsole: Boolean get() = sender === ProxyServer.getInstance().console
    val server: net.md_5.bungee.api.connection.Server? get() = (sender as? ProxiedPlayer)?.server
}

inline class BukkitPlatformSender(val sender: org.bukkit.command.CommandSender): PlatformSender {
    override val base: Any get() = sender
    override val name: String get() = sender.name
    override fun sendMessage(vararg components: BaseComponent) = sender.sendMessage(*components)
    override fun hasPermission(node: String): Boolean = sender.hasPermission(node)
    override val isConsole: Boolean get() = sender === sender.server.consoleSender
}

inline class BungeeEvent(private val event: net.md_5.bungee.api.plugin.Cancellable): PlatformEvent {
    override var isCancelled: Boolean
        get() = event.isCancelled
        set(value) { event.isCancelled = value }
}

inline class BukkitEvent(private val event: org.bukkit.event.Cancellable): PlatformEvent {
    override var isCancelled: Boolean
        get() = event.isCancelled
        set(value) { event.isCancelled = value }
}