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

package eu.mikroskeem.adminchatter.bukkit.commands

import eu.mikroskeem.adminchatter.bukkit.BukkitPlatformSender
import eu.mikroskeem.adminchatter.bukkit.plugin
import eu.mikroskeem.adminchatter.common.config.ChannelCommandInfo
import eu.mikroskeem.adminchatter.common.platform.config
import eu.mikroskeem.adminchatter.common.sendChannelChat
import eu.mikroskeem.adminchatter.common.utils.BASE_CHAT_PERMISSION
import eu.mikroskeem.adminchatter.common.utils.PLUGIN_CHANNEL_PROXY
import eu.mikroskeem.adminchatter.common.utils.passMessage
import eu.mikroskeem.adminchatter.common.utils.serializeProxyChat
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * @author Mark Vainomaa
 */
class AdminchatterChatCommand(private val info: ChannelCommandInfo): Command(info.commandName) {
    init {
        permission = BASE_CHAT_PERMISSION + info.channelName
        aliases = info.commandAliases
    }

    override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (!testPermission(sender))
            return true

        val platformSender = BukkitPlatformSender(sender)
        if (plugin.isBehindProxy) {
            val isConsole = sender !is Player
            val messageSender = if (isConsole) {
                plugin.server.onlinePlayers.firstOrNull()
            } else {
                sender as Player
            } ?: run {
                plugin.slF4JLogger.warn("Failed to send proxied chat - server has no online players!")
                return true
            }

            val serialized = serializeProxyChat(info, sender.name, isConsole, args.joinToString(separator = " "))
            messageSender.sendPluginMessage(plugin, PLUGIN_CHANNEL_PROXY, serialized)
        } else {
            if (sender !is Player && !config.allowConsoleUsage) {
                platformSender.passMessage(config.messages.channelChatIsOnlyForPlayers)
                return true
            }

            platformSender.sendChannelChat(info, args.joinToString(separator = " "))
        }

        return true
    }
}

class AdminchatterToggleCommand(private val info: ChannelCommandInfo): Command(info.toggleCommandName) {
    init {
        permission = BASE_CHAT_PERMISSION + info.channelName
        aliases = info.toggleCommandAliases
    }

    override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if(!testPermission(sender))
            return true

        val platformSender = BukkitPlatformSender(sender)
        if(sender !is Player) {
            platformSender.passMessage(config.messages.togglingIsOnlyForPlayers)
            return true
        }

        // Get player's current channel
        val currentChannel = platformSender.currentChannel

        // If player has no channel
        when {
            currentChannel == null -> {
                platformSender.currentChannel = info
                platformSender.passMessage(config.messages.toggledOn, info)
            }

            currentChannel.channelName == info.channelName -> {
                // If toggled channel equals to one representing a command, untoggle
                platformSender.currentChannel = null
                platformSender.passMessage(config.messages.toggledOff, info)
            }

            else -> {
                // If toggled channel is not same as one representing a command, switch channel
                platformSender.currentChannel = info
                platformSender.passMessage(config.messages.channelSwitched, info)
            }
        }

        return true
    }
}