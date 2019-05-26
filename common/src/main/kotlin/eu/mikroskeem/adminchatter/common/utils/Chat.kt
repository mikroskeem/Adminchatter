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

package eu.mikroskeem.adminchatter.common.utils

import eu.mikroskeem.adminchatter.common.config.ChannelCommandInfo
import eu.mikroskeem.adminchatter.common.platform.PlatformSender
import eu.mikroskeem.adminchatter.common.platform.config
import net.kyori.text.serializer.legacy.LegacyComponentSerializer

/**
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

fun String.colored(): String = this.replace('&', '§') // TODO: less safe than ChatColor utility

fun PlatformSender.passMessage(message: String, channel: ChannelCommandInfo? = null) {
    sendMessage(LegacyComponentSerializer.INSTANCE.deserialize(message.replacePlaceholders(name, message, serverName, channel?.prettyChannelName)))
}