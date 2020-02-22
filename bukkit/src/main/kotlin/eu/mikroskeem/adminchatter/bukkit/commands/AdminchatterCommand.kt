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
import eu.mikroskeem.adminchatter.common.platform.config
import eu.mikroskeem.adminchatter.common.utils.passMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * @author Mark Vainomaa
 */
class AdminchatterCommand: CommandExecutor {
    override fun onCommand(_sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val sender = BukkitPlatformSender(_sender)

        plugin.configLoader.load()
        plugin.configLoader.save()
        plugin.setupChannels()
        sender.passMessage(config.messages.pluginConfigurationReloaded)
        return true
    }
}