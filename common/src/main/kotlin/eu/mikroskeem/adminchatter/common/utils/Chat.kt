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

package eu.mikroskeem.adminchatter.common.utils

import eu.mikroskeem.adminchatter.common.config.ChannelCommandInfo
import eu.mikroskeem.adminchatter.common.platform.PlatformSender
import eu.mikroskeem.adminchatter.common.platform.config
import net.kyori.text.Component
import net.kyori.text.TextComponent
import net.kyori.text.event.ClickEvent
import net.kyori.text.format.Style
import net.kyori.text.serializer.legacy.LegacyComponentSerializer
import java.util.ArrayDeque
import java.util.Queue
import java.util.regex.Matcher
import java.util.regex.Pattern

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
    sendMessage(LegacyComponentSerializer.INSTANCE.deserialize(message.replacePlaceholders(name, message, serverName, channel?.prettyChannelName)).injectLinks())
}

val URL_PATTERN = Pattern.compile("(?:(https?)://)?([-\\w_.]+\\.\\w{2,})(/\\S*)?")

// https://github.com/KyoriPowered/text/pull/34/commits/ce66dbcd9b3da538b942d17d18b77f8fc002f66a
fun TextComponent.injectLinks(linkStyle: Style? = null): TextComponent {
    val produced: MutableList<Component> = ArrayList()
    val queue: Queue<TextComponent> = ArrayDeque()
    queue.add(this)

    while(!queue.isEmpty()) {
        val current: TextComponent  = queue.remove()
        val content: String = current.content()
        val matcher: Matcher = URL_PATTERN.matcher(content)
        val withoutChildren: TextComponent = current.children(emptyList())

        if(matcher.find()) {
            var lastEnd = 0
            do {
                val start = matcher.start()
                val end = matcher.end()
                val matched = matcher.group()

                val prefix = content.substring(lastEnd, start)
                if(prefix.isNotEmpty())
                    produced.add(withoutChildren.content(prefix))

                val style = Style.builder()
                        .clickEvent(ClickEvent.openUrl(matched))
                        .hoverEvent(linkStyle?.hoverEvent())
                        .color(linkStyle?.color() ?: withoutChildren.color())
                        .build()

                val link = withoutChildren
                        .content(matched)
                        .style(style)
                produced.add(link)
                lastEnd = end
            } while(matcher.find())

            if(content.length - lastEnd > 0)
                produced.add(withoutChildren.content(content.substring(lastEnd)))
        } else {
            // children are handled separately
            produced.add(withoutChildren)
        }

        current.children()
                .map { it as TextComponent } // we can guarantee that all children are TextComponents
                .forEach { queue.add(it) }
    }

    return if(produced.size == 1) {
        produced[0] as TextComponent
    } else {
        val children = produced.subList(1, produced.size)
        return produced[0].children(children) as TextComponent
    }
}