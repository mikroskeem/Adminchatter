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

package eu.mikroskeem.adminchatter.common

import eu.mikroskeem.adminchatter.common.config.ChannelCommandInfo
import eu.mikroskeem.adminchatter.common.platform.PlatformEvent
import eu.mikroskeem.adminchatter.common.platform.PlatformSender
import eu.mikroskeem.adminchatter.common.platform.config
import eu.mikroskeem.adminchatter.common.platform.currentPlatform
import eu.mikroskeem.adminchatter.common.utils.BASE_CHAT_PERMISSION
import eu.mikroskeem.adminchatter.common.utils.injectLinks
import eu.mikroskeem.adminchatter.common.utils.passMessage
import eu.mikroskeem.adminchatter.common.utils.replacePlaceholders
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

/**
 * @author Mark Vainomaa
 */
// Broadcasts admin chat message
fun PlatformSender.sendChannelChat(info: ChannelCommandInfo, message: String) {
    // Do not process empty message
    if(message.isEmpty()) {
        passMessage(config.messages.mustSupplyAMessage)
        return
    }

    val chatFormat = info.messageFormat.takeUnless { it.isEmpty() } ?: return // User did not set chat format, don't process anything
    val senderName = if(isConsole) (config.consoleName.takeUnless { it.isEmpty() } ?: name) else name
    val serverName = if(currentPlatform.isProxy) serverName else (config.noneServerName.takeUnless { it.isEmpty() } ?: "none")

    // Start building chat component
    val buildableComponent = TextComponent.builder("")

    // Build hover event
    info.messageHoverText.takeUnless { it.isEmpty() }?.run {
        val text = this.replacePlaceholders(senderName, message, serverName, info.channelName)
        buildableComponent.hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, LegacyComponentSerializer.legacyAmpersand().deserialize(text)))
    }

    // Build command event
    info.clickCommand.takeUnless { it.isEmpty() }?.run {
        val command = this.replacePlaceholders(senderName, message, serverName, info.channelName)
        buildableComponent.clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND, command))
    }

    // Add remaining text
    buildableComponent.append(LegacyComponentSerializer.legacySection().deserialize(
            chatFormat.replacePlaceholders(senderName, message, serverName, info.channelName)
    ))

    // Replace url components hover text
    val linkHover = config.messages.urlHoverText.takeUnless { it.isEmpty() }?.replacePlaceholders(senderName, message, serverName, info.channelName)?.run {
        HoverEvent.of(HoverEvent.Action.SHOW_TEXT, LegacyComponentSerializer.legacyAmpersand().deserialize(this))
    }

    val component = buildableComponent.build().injectLinks(linkHover)

    // Send message
    val sound: ByteArray? = info.soundEffect.takeIf { it.isNotEmpty() }?.toByteArray()
    currentPlatform.onlinePlayers.filter { it.hasPermission(BASE_CHAT_PERMISSION + info.channelName) }.forEach {
        sound?.run {
            it.playSound(sound)
        }
        it.sendMessage(component)
    }

    // Send message to console as well, if configured so
    if(config.allowConsoleUsage)
        currentPlatform.consoleSender.sendMessage(component)
}

// Handles chat events
fun handleToggleChat(event: PlatformEvent, sender: PlatformSender, chatMessage: String) {
    var message = chatMessage

    // Figure out what channel is player in and check if player has channel toggle
    var wasToggle = false
    val channel: ChannelCommandInfo = if(sender.currentChannel != null) {
        wasToggle = true
        sender.currentChannel!!
    } else {
        // Find channel by prefix what sender is using, or return
        channelsByChatPrefix.filterKeys { message.startsWith(it) }
                .takeIf { it.isNotEmpty() }
                ?.values?.firstOrNull()
                ?: return
    }

    // Check if player has permission for given channel
    if(!sender.hasPermission(BASE_CHAT_PERMISSION + channel.channelName))
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

    // Cancel event as message shouldn't reach to backend server or chat
    event.isCancelled = true

    // Send message to channel
    sender.sendChannelChat(channel, message)
}