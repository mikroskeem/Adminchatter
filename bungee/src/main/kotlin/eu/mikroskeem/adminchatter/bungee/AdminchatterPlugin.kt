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

import eu.mikroskeem.adminchatter.bungee.commands.AdminchatCommand
import eu.mikroskeem.adminchatter.bungee.commands.AdminchatToggleCommand
import eu.mikroskeem.adminchatter.bungee.commands.AdminchatterCommand
import eu.mikroskeem.adminchatter.bungee.listeners.ChannelListener
import eu.mikroskeem.adminchatter.bungee.listeners.ChatListener
import eu.mikroskeem.adminchatter.common.ConfigurationLoader
import eu.mikroskeem.adminchatter.common.adminchatTogglePlayers
import eu.mikroskeem.adminchatter.common.channelsByChatPrefix
import eu.mikroskeem.adminchatter.common.channelsByName
import eu.mikroskeem.adminchatter.common.config.AdminchatterConfig
import eu.mikroskeem.adminchatter.common.config.CONFIGURATION_FILE_HEADER
import eu.mikroskeem.adminchatter.common.config.ChannelCommandInfo
import eu.mikroskeem.adminchatter.common.platform.config
import eu.mikroskeem.adminchatter.common.platform.currentPlatform
import eu.mikroskeem.adminchatter.common.utils.injectBetterUrlPattern
import eu.mikroskeem.adminchatter.common.utils.passMessage
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Plugin
import org.bstats.bungeecord.MetricsLite

/**
 * Adminchatter plugin
 *
 * @author Mark Vainomaa
 */
class AdminchatterPlugin: Plugin() {
    lateinit var configLoader: ConfigurationLoader<AdminchatterConfig>
        private set

    private val registeredCommands = ArrayList<Command>()

    override fun onLoad() {
        currentPlatform = BungeePlatform(this)
    }

    override fun onEnable() {
        configLoader = ConfigurationLoader(
                dataFolder.toPath().resolve("config.cfg"),
                AdminchatterConfig::class.java,
                header = CONFIGURATION_FILE_HEADER
        )

        try {
            injectBetterUrlPattern()
        } catch (e: Exception) {
            logger.warning("Failed to inject improved URL regex into TextComponent class. URLs with " +
                    "extremely short domain names may not work!")
            e.printStackTrace()
        }

        setupChannels()
        registerCommand(AdminchatterCommand::class)
        registerListener(ChatListener::class)
        currentPlatform.registerInternalListener(ChannelListener())

        eu.mikroskeem.adminchatter.bungee.proxy.scheduler.runAsync(this) {
            MetricsLite(this)
        }
    }

    fun setupChannels() {
        // Clean up channels and unregister commands
        channelsByName.clear()
        channelsByChatPrefix.clear()

        if(registeredCommands.isNotEmpty())
            registeredCommands.forEach(eu.mikroskeem.adminchatter.bungee.proxy.pluginManager::unregisterCommand)

        // Store player toggled channels
        val playerToggledChannels = HashMap<CommandSender, ChannelCommandInfo>()
        proxy.players.map { BungeePlatformSender(it) }
                .filter { it.currentChannel != null }
                .forEach {
            playerToggledChannels[it.base as CommandSender] = it.currentChannel!!
        }

        // Register new channels and commands
        config.channels.forEach { channel ->
            if(!channel.isValid()) {
                logger.warning("Channel $channel is invalid! Skipping")
                return@forEach
            }

            channelsByName[channel.channelName] = channel
            if(channel.messagePrefix.isNotEmpty()) {
                channelsByChatPrefix[channel.messagePrefix] = channel
            }

            // Register commands
            registeredCommands.add(registerCommand(AdminchatCommand(channel)))
            registeredCommands.add(registerCommand(AdminchatToggleCommand(channel)))
        }

        // Populate toggle channel list again for players
        playerToggledChannels.forEach { _sender, (channelName, prettyChannelName) ->
            val sender = BungeePlatformSender(_sender)
            val channel = channelsByName[channelName] ?: run {
                sender.passMessage(config.messages.toggledChannelDoesNotExistAnymore.replace("{pretty_channel_name}", prettyChannelName), null)
                sender.currentChannel = null
                return@forEach
            }

            adminchatTogglePlayers[sender.base] = channel
        }
    }
}