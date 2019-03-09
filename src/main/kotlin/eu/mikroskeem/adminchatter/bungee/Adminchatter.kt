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
import eu.mikroskeem.adminchatter.common.utils.PLUGIN_CHANNEL_SOUND
import eu.mikroskeem.adminchatter.common.utils.colored
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.WeakHashMap

/**
 * Main plugin logic
 *
 * @author Mark Vainomaa
 */
fun String.replacePlaceholders(playerName: String? = null,
                               message: String? = null,
                               serverName: String? = null,
                               channelName: String? = null): String {
    return this.colored()
            .replace("{plugin_prefix}", config.messages.messagePrefix.colored())
            .replace("{player_name}", playerName ?: "")
            .replace("{message}", message ?: "")
            .replace("{colored_message}", message?.colored() ?: "")
            .replace("{channel_name}", channelName?.colored() ?: "")
            .replace("{server_name}", serverName ?: "")
            .replace("{pretty_server_name}", serverName?.run(config.prettyServerNames::get)?.colored() ?: serverName ?: "")
}

fun CommandSender.passMessage(message: String, channel: ChannelCommandInfo? = null) {
    sendMessage(*TextComponent.fromLegacyText(message.replacePlaceholders(name, message, (this as? ProxiedPlayer)?.server?.info?.name, channel?.prettyChannelName)))
}

// Stores players who have adminchat toggle on. Cleans up itself, as it is backed by WeakHashMap
val adminchatTogglePlayers = WeakHashMap<ProxiedPlayer, ChannelCommandInfo>()

// Broadcasts admin chat message
internal fun CommandSender.sendChannelChat(info: ChannelCommandInfo, message: String) {
    // Do not process empty message
    if(message.isEmpty()) {
        passMessage(config.messages.mustSupplyAMessage)
        return
    }

    val chatFormat = info.messageFormat.takeUnless { it.isEmpty() } ?: return // User did not set chat format, don't process anything
    val senderName = (this as? ProxiedPlayer)?.name ?: (config.consoleName.takeUnless { it.isEmpty() } ?: "CONSOLE")
    val serverName = if(this is ProxiedPlayer) server.info?.name else (config.noneServerName.takeUnless { it.isEmpty() } ?: "none")

    // Start building chat component
    val baseComponent = TextComponent()

    // Build hover event
    info.messageHoverText.takeUnless { it.isEmpty() }?.run {
        val text = this.replacePlaceholders(senderName, message, serverName, info.channelName)
        baseComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(text))
    }

    // Build command event
    info.clickCommand.takeUnless { it.isEmpty() }?.run {
        val command = this.replacePlaceholders(senderName, message, serverName, info.channelName)
        baseComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, command)
    }

    // Add remaining text
    TextComponent.fromLegacyText(chatFormat.replacePlaceholders(senderName, message, serverName, info.channelName)).forEach {
        baseComponent.addExtra(it)
    }

    // Replace url components hover text
    config.messages.urlHoverText.takeUnless { it.isEmpty() }?.replacePlaceholders(senderName, message, serverName, info.channelName)?.let { urlText ->
        baseComponent.extra.filter { it.clickEvent?.action == OPEN_URL }.forEach {
            it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(urlText))
        }
    }

    // Send message
    val sound: ByteArray? = info.soundEffect.takeIf { it.isNotEmpty() }?.toByteArray()
    proxy.players.filter { it.hasPermission(BASE_CHAT_PERMISSION + info.channelName) }.forEach {
        sound?.run { it.server.sendData(PLUGIN_CHANNEL_SOUND, this) }
        it.sendMessage(baseComponent)
    }

    // Send message to console as well, if configured so
    if(config.allowConsoleUsage)
        proxy.console.sendMessage(baseComponent)
}