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
import eu.mikroskeem.adminchatter.common.platform.config
import eu.mikroskeem.adminchatter.common.sendChannelChat
import eu.mikroskeem.adminchatter.common.utils.ADMINCHATTER_COMMAND_PERMISSION
import eu.mikroskeem.adminchatter.common.utils.BASE_CHAT_PERMISSION
import eu.mikroskeem.adminchatter.common.utils.passMessage
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command

/**
 * @author Mark Vainomaa
 */
class AdminchatterCommand: Command("adminchatter", ADMINCHATTER_COMMAND_PERMISSION) {
    override fun execute(_sender: CommandSender, args: Array<out String>) {
        val sender = BungeePlatformSender(_sender)

        plugin.configLoader.load()
        plugin.configLoader.save()
        plugin.setupChannels()
        sender.passMessage(config.messages.pluginConfigurationReloaded)
    }
}

class AdminchatCommand(private val info: ChannelCommandInfo): Command(info.commandName, BASE_CHAT_PERMISSION + info.channelName, *info.commandAliases.toTypedArray()) {
    override fun execute(_sender: CommandSender, args: Array<out String>) {
        val sender = BungeePlatformSender(_sender)
        if(_sender !is ProxiedPlayer && !config.allowConsoleUsage) {
            sender.passMessage(config.messages.channelChatIsOnlyForPlayers)
            return
        }

        sender.sendChannelChat(info, args.joinToString(separator = " "))
    }
}

class AdminchatToggleCommand(private val info: ChannelCommandInfo): Command(info.toggleCommandName, BASE_CHAT_PERMISSION + info.channelName, *info.toggleCommandAliases.toTypedArray()) {
    override fun execute(_sender: CommandSender, args: Array<out String>) {
        val sender = BungeePlatformSender(_sender)

        if(_sender !is ProxiedPlayer) {
            sender.passMessage(config.messages.togglingIsOnlyForPlayers)
            return
        }

        // Get player's current channel
        val currentChannel = sender.currentChannel

        // If player has no channel
        when {
            currentChannel == null -> {
                sender.currentChannel = info
                sender.passMessage(config.messages.toggledOn, info)
            }

            currentChannel.channelName == info.channelName -> {
                // If toggled channel equals to one representing a command, untoggle
                sender.currentChannel = null
                sender.passMessage(config.messages.toggledOff, info)
            }

            else -> {
                // If toggled channel is not same as one representing a command, switch channel
                sender.currentChannel = info
                sender.passMessage(config.messages.channelSwitched, info)
            }
        }
    }
}