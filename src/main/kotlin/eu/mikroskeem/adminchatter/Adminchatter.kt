/*
 * This file is part of project Adminchatter, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2018 Mark Vainomaa <mikroskeem@mikroskeem.eu>
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

package eu.mikroskeem.adminchatter

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import java.util.Collections
import java.util.WeakHashMap
import kotlin.reflect.KClass

/**
 * Main plugin logic
 *
 * @author Mark Vainomaa
 */

// Shortcuts
val proxy: ProxyServer get() = ProxyServer.getInstance()
val plugin: AdminchatterPlugin get() = proxy.pluginManager.getPlugin("Adminchatter") as AdminchatterPlugin
val config: Adminchatter get() = plugin.configLoader.configuration

// Utility functions
fun <T: Listener> Plugin.registerListener(listenerClass: KClass<T>) {
    proxy.pluginManager.registerListener(this, listenerClass.java.getConstructor().newInstance())
}

fun <T: Command> Plugin.registerCommand(command: T): T {
    proxy.pluginManager.registerCommand(this, command)
    return command
}

fun <T: Command> Plugin.registerCommand(commandClass: KClass<T>): T {
    return registerCommand(commandClass.java.getConstructor().newInstance())
}

fun String.colored(): String = ChatColor.translateAlternateColorCodes('&', this)

fun String.replacePlaceholders(playerName: String? = null,
                               message: String? = null,
                               serverName: String? = null): String {
    return this.colored()
            .replace("{plugin_prefix}", config.messages.messagePrefix.colored())
            .replace("{player_name}", playerName ?: "")
            .replace("{message}", message ?: "")
            .replace("{colored_message}", message?.colored() ?: "")
            .replace("{server_name}", serverName ?: "")
            .replace("{pretty_server_name}", serverName?.run(config.prettyServerNames::get)?.colored() ?: serverName ?: "")
}

fun CommandSender.passMessage(message: String) {
    sendMessage(*TextComponent.fromLegacyText(message.replacePlaceholders()))
}

// Configuration file header
const val CONFIGURATION_FILE_HEADER = """
 An awesome adminchat plugin, made with ♥ by mikroskeem

 Globally supported placeholders:
 - {plugin_prefix} -> plugin message prefix

 Placeholders, which are only available on adminchat messages:
 - {player_name} -> pretty obvious
 - {message} -> pretty obvious again
 - {colored_message} -> message, now just with fancy colors what players may add
 - {server_name} -> server where given player sent the adminchat message. For console, 'none' is used
 - {pretty_server_name} -> see above, just server name from configuration option `pretty-server-names` is used instead
"""

// Permission nodes
const val CHAT_PERMISSION = "adminchatter.chat"
const val ADMINCHATTER_COMMAND_PERMISSION = "adminchatter.reload"

// Stores players who have adminchat toggle on. Cleans up itself, as it is backed by WeakHashMap
val adminchatTogglePlayers: MutableSet<ProxiedPlayer> = Collections.newSetFromMap(WeakHashMap())

// Broadcasts admin chat message
internal fun sendAdminChat(sender: CommandSender, message: String) {
    // Do not process empty message
    if(message.isEmpty()) {
        sender.passMessage(config.messages.mustSupplyAMessage)
        return
    }

    val chatFormat = config.adminChatFormat.takeUnless { it.isEmpty() } ?: return // User did not set chat format, don't process anything
    val senderName = if(sender is ProxiedPlayer) sender.name else (config.consoleName.takeUnless { it.isEmpty() } ?: "CONSOLE")
    val serverName = if(sender is ProxiedPlayer) sender.server.info.name else (config.noneServerName.takeUnless { it.isEmpty() } ?: "none")

    // Start building chat component
    val baseComponent = TextComponent()

    // Build hover event
    config.adminChatHoverText.takeUnless { it.isEmpty() }?.run {
        val text = this.replacePlaceholders(senderName, message, serverName)
        baseComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(text))
    }

    // Build command event
    config.adminChatClickCommand.takeUnless { it.isEmpty() }?.run {
        val command = this.replacePlaceholders(senderName, message, serverName)
        baseComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, command)
    }

    // Add remaining text
    TextComponent.fromLegacyText(chatFormat.replacePlaceholders(senderName, message, serverName)).forEach {
        baseComponent.addExtra(it)
    }

    // Send message
    proxy.players.filter { it.hasPermission(CHAT_PERMISSION) }.forEach { it.sendMessage(baseComponent) }

    // Send message to console as well, if configured so
    if(config.allowConsoleUsage)
        proxy.console.sendMessage(baseComponent)
}