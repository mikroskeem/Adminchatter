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

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command

/**
 * @author Mark Vainomaa
 */
class AdminchatterCommand: Command("adminchatter", ADMINCHATTER_COMMAND_PERMISSION) {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        plugin.configLoader.load()
        plugin.configLoader.save()
        plugin.setupCommands()
        sender.passMessage(config.messages.pluginConfigurationReloaded)
    }
}

class AdminchatCommand(commandName: String, aliases: Array<out String>): Command(commandName, CHAT_PERMISSION, *aliases) {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if(sender !is ProxiedPlayer && !config.allowConsoleUsage) {
            sender.passMessage(config.messages.adminChatIsOnlyForPlayers)
            return
        }

        sender.sendAdminChat(args.joinToString(separator = " "))
    }
}

class AdminchatToggleCommand(commandName: String, aliases: Array<out String>): Command(commandName, CHAT_PERMISSION, *aliases) {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if(sender !is ProxiedPlayer) {
            sender.passMessage(config.messages.adminChatTogglingIsOnlyForPlayers)
            return
        }

        if(adminchatTogglePlayers.contains(sender)) {
            adminchatTogglePlayers.remove(sender)
            sender.passMessage(config.messages.adminChatToggleDisabled)
        } else {
            adminchatTogglePlayers.add(sender)
            sender.passMessage(config.messages.adminChatToggleEnabled)
        }
    }
}