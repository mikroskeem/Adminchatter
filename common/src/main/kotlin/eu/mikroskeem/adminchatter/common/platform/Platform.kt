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

package eu.mikroskeem.adminchatter.common.platform

import eu.mikroskeem.adminchatter.common.adminchatTogglePlayers
import eu.mikroskeem.adminchatter.common.config.AdminchatterConfig
import eu.mikroskeem.adminchatter.common.config.ChannelCommandInfo
import eu.mikroskeem.adminchatter.common.eventBus
import eu.mikroskeem.adminchatter.common.events.ChannelToggleEvent
import net.kyori.adventure.text.Component
import kotlin.properties.Delegates

/**
 * @author Mark Vainomaa
 */
var currentPlatform: Platform by Delegates.notNull()
val config: AdminchatterConfig get() = currentPlatform.config

interface Platform {
    val onlinePlayers: Collection<PlatformSender>
    val isProxy: Boolean
    val consoleSender: PlatformSender
    val config: AdminchatterConfig

    fun registerInternalListener(listener: Any) {
        eventBus.register(listener)
    }
}

interface PlatformSender {
    val base: Any
    val name: String
    fun sendMessage(component: Component)
    fun hasPermission(node: String): Boolean
    val isConsole: Boolean
    var currentChannel: ChannelCommandInfo?
        get() = adminchatTogglePlayers[base]
        set(value) {
            val previousChannel = adminchatTogglePlayers[base]
            eventBus.post(ChannelToggleEvent(this, previousChannel, value))

            if(value == null) {
                adminchatTogglePlayers.remove(base)
            } else {
                adminchatTogglePlayers[base] = value
            }
        }
    val serverName: String get() = ""
    fun playSound(soundData: ByteArray)
}

interface PlatformEvent {
    var isCancelled: Boolean
}