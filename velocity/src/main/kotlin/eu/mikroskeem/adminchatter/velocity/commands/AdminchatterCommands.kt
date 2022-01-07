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

package eu.mikroskeem.adminchatter.velocity.commands

import com.velocitypowered.api.command.Command
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import eu.mikroskeem.adminchatter.common.config.ChannelCommandInfo
import eu.mikroskeem.adminchatter.common.platform.config
import eu.mikroskeem.adminchatter.common.sendChannelChat
import eu.mikroskeem.adminchatter.common.utils.ADMINCHATTER_COMMAND_PERMISSION
import eu.mikroskeem.adminchatter.common.utils.BASE_CHAT_PERMISSION
import eu.mikroskeem.adminchatter.common.utils.passMessage
import eu.mikroskeem.adminchatter.velocity.VelocityPlatformSender
import eu.mikroskeem.adminchatter.velocity.plugin

/**
 * @author Mark Vainomaa
 */
class AdminchatterCommand: SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val sender = VelocityPlatformSender(invocation.source())

        plugin.configLoader.load()
        plugin.configLoader.save()
        plugin.setupChannels()
        sender.passMessage(config.messages.pluginConfigurationReloaded)
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission(ADMINCHATTER_COMMAND_PERMISSION)
}

class AdminchatCommand(private val info: ChannelCommandInfo): SimpleCommand {
    private val permissionNode = BASE_CHAT_PERMISSION + info.channelName

    override fun execute(invocation: SimpleCommand.Invocation) {
        val sender = VelocityPlatformSender(invocation.source())
        if(invocation.source() !is Player && !config.allowConsoleUsage) {
            sender.passMessage(config.messages.channelChatIsOnlyForPlayers)
            return
        }

        sender.sendChannelChat(info, invocation.arguments().joinToString(separator = " "))
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission(permissionNode)
}

class AdminchatToggleCommand(private val info: ChannelCommandInfo): SimpleCommand {
    private val permissionNode = BASE_CHAT_PERMISSION + info.channelName

    override fun execute(invocation: SimpleCommand.Invocation) {
        val sender = VelocityPlatformSender(invocation.source())

        if(invocation.source() !is Player) {
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

    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission(permissionNode)
}