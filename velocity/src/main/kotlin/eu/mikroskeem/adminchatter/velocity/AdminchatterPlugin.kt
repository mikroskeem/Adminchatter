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

package eu.mikroskeem.adminchatter.velocity

import com.google.inject.Inject
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import eu.mikroskeem.adminchatter.common.ConfigurationLoader
import eu.mikroskeem.adminchatter.common.adminchatTogglePlayers
import eu.mikroskeem.adminchatter.common.channelsByChatPrefix
import eu.mikroskeem.adminchatter.common.channelsByName
import eu.mikroskeem.adminchatter.common.config.AdminchatterConfig
import eu.mikroskeem.adminchatter.common.config.CONFIGURATION_FILE_HEADER
import eu.mikroskeem.adminchatter.common.config.ChannelCommandInfo
import eu.mikroskeem.adminchatter.common.platform.config
import eu.mikroskeem.adminchatter.common.platform.currentPlatform
import eu.mikroskeem.adminchatter.common.utils.passMessage
import eu.mikroskeem.adminchatter.velocity.commands.AdminchatCommand
import eu.mikroskeem.adminchatter.velocity.commands.AdminchatToggleCommand
import eu.mikroskeem.adminchatter.velocity.commands.AdminchatterCommand
import eu.mikroskeem.adminchatter.velocity.listeners.ChannelListener
import eu.mikroskeem.adminchatter.velocity.listeners.ChatListener
import org.slf4j.Logger
import java.nio.file.Path
import java.util.LinkedList
import kotlin.properties.Delegates

/**
 * @author Mark Vainomaa
 */
@Plugin(id = "adminchatter",
        name = "Adminchatter",
        version = "0.0.11-SNAPSHOT",
        description = "An adminchat plugin",
        authors = ["mikroskeem"])
class AdminchatterPlugin {
    @Inject lateinit var server: ProxyServer
        private set
    @Inject lateinit var logger: Logger
        private set
    @Inject @DataDirectory private lateinit var dataFolderPath: Path

    private val registeredCommands = LinkedList<String>()
    lateinit var configLoader: ConfigurationLoader<AdminchatterConfig>
        private set

    @Subscribe
    fun on(event: ProxyInitializeEvent) {
        plugin = this
        currentPlatform = VelocityPlatform(this)

        configLoader = ConfigurationLoader(
                dataFolderPath.resolve("config.cfg"),
                AdminchatterConfig::class.java,
                header = CONFIGURATION_FILE_HEADER
        )

        setupChannels()
        registerCommand<AdminchatterCommand>("adminchatter")
        registerListener<ChatListener>()
        currentPlatform.registerInternalListener(ChannelListener())
    }

    internal fun setupChannels() {
        // Clean up channels and unregister commands
        channelsByName.clear()
        channelsByChatPrefix.clear()

        if(registeredCommands.isNotEmpty())
            registeredCommands.forEach(server.commandManager::unregister)

        // Store player toggled channels
        val playerToggledChannels = HashMap<CommandSource, ChannelCommandInfo>()
        server.allPlayers.map { VelocityPlatformSender(it) }
                .filter { it.currentChannel != null }
                .forEach {
                    playerToggledChannels[it.base as CommandSource] = it.currentChannel!!
                }

        // Register new channels and commands
        config.channels.forEach { channel ->
            if(!channel.isValid()) {
                logger.warn("Channel {} is invalid! Skipping", channel)
                return@forEach
            }

            channelsByName[channel.channelName] = channel
            if(channel.messagePrefix.isNotEmpty()) {
                channelsByChatPrefix[channel.messagePrefix] = channel
            }

            // Register commands
            registeredCommands.add(registerCommand(AdminchatCommand(channel), channel.commandName, channel.commandAliases))
            registeredCommands.add(registerCommand(AdminchatToggleCommand(channel), channel.toggleCommandName, channel.toggleCommandAliases))
        }

        // Populate toggle channel list again for players
        playerToggledChannels.forEach { (_sender, oldChannel) ->
            val sender = VelocityPlatformSender(_sender)
            val channel = channelsByName[oldChannel.channelName] ?: run {
                sender.passMessage(config.messages.toggledChannelDoesNotExistAnymore, oldChannel)
                sender.passMessage(currentPlatform.config.messages.toggledOff, oldChannel)
                sender.currentChannel = null
                return@forEach
            }

            adminchatTogglePlayers[sender.base] = channel
        }
    }
}

var plugin: AdminchatterPlugin by Delegates.notNull()