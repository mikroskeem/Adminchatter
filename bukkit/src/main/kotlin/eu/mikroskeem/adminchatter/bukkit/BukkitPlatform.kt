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

package eu.mikroskeem.adminchatter.bukkit

import eu.mikroskeem.adminchatter.common.config.AdminchatterConfig
import eu.mikroskeem.adminchatter.common.platform.Platform
import eu.mikroskeem.adminchatter.common.platform.PlatformEvent
import eu.mikroskeem.adminchatter.common.platform.PlatformSender
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable

/**
 * @author Mark Vainomaa
 */
class BukkitPlatform(private val plugin: AdminchatterPlugin): Platform {
    override val onlinePlayers: Collection<PlatformSender> get() = plugin.server.onlinePlayers.map { BukkitPlatformSender(it) }
    override val isProxy: Boolean get() = false
    override val consoleSender: PlatformSender get() = BukkitPlatformSender(plugin.server.consoleSender)
    override val config: AdminchatterConfig get() = plugin.configLoader.configuration
}

class BukkitPlatformSender(val sender: CommandSender): PlatformSender {
    override val base: Any get() = sender
    override val name: String get() = sender.name
    override fun sendMessage(component: Component) = audiences.sender(sender).sendMessage(Identity.nil(), component)
    override fun hasPermission(node: String): Boolean = sender.hasPermission(node)
    override val isConsole: Boolean get() = sender === sender.server.consoleSender
    override fun playSound(soundData: ByteArray) { (sender as? Player)?.playSound(String(soundData)) }

    companion object {
        val audiences = BukkitAudiences.create(plugin)
    }
}

class BukkitEvent(private val event: Cancellable): PlatformEvent {
    override var isCancelled: Boolean
        get() = event.isCancelled
        set(value) { event.isCancelled = value }
}
