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

package eu.mikroskeem.adminchatter.bukkit

import eu.mikroskeem.adminchatter.bukkit.commands.AdminchatterChatCommand
import eu.mikroskeem.adminchatter.bukkit.commands.AdminchatterCommand
import eu.mikroskeem.adminchatter.bukkit.commands.AdminchatterToggleCommand
import eu.mikroskeem.adminchatter.bukkit.listeners.ChannelListener
import eu.mikroskeem.adminchatter.bukkit.listeners.ChatListener
import eu.mikroskeem.adminchatter.common.ConfigurationLoader
import eu.mikroskeem.adminchatter.common.adminchatTogglePlayers
import eu.mikroskeem.adminchatter.common.channelsByChatPrefix
import eu.mikroskeem.adminchatter.common.channelsByName
import eu.mikroskeem.adminchatter.common.config.AdminchatterConfig
import eu.mikroskeem.adminchatter.common.config.CONFIGURATION_FILE_HEADER
import eu.mikroskeem.adminchatter.common.config.ChannelCommandInfo
import eu.mikroskeem.adminchatter.common.platform.currentPlatform
import eu.mikroskeem.adminchatter.common.utils.PLUGIN_CHANNEL_PROXY
import eu.mikroskeem.adminchatter.common.utils.PLUGIN_CHANNEL_SOUND
import eu.mikroskeem.adminchatter.common.utils.passMessage
import org.bstats.bukkit.MetricsLite
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.LinkedList
import java.util.Locale

/**
 * @author Mark Vainomaa
 */
class AdminchatterPlugin: JavaPlugin() {
    private val registeredCommands = LinkedList<Command>()
    lateinit var configLoader: ConfigurationLoader<AdminchatterConfig>
        private set

    override fun onLoad() {
        currentPlatform = BukkitPlatform(this)
    }

    override fun onEnable() {
        server.scheduler.runTaskAsynchronously(this, Runnable {
            MetricsLite(this)
        })

        configLoader = ConfigurationLoader(
                dataFolder.toPath().resolve("config.cfg"),
                AdminchatterConfig::class.java,
                header = CONFIGURATION_FILE_HEADER
        )

        registerCommand<AdminchatterCommand>("adminchatter")
        setupChannels()

        if (isBehindProxy) {
            logger.info("BungeeCord mode - listening for sound notification messages and proxying chat")
            server.messenger.registerIncomingPluginChannel(this, PLUGIN_CHANNEL_SOUND, this::processMessage)
            server.messenger.registerOutgoingPluginChannel(this, PLUGIN_CHANNEL_PROXY)
        } else {
            registerListener<ChatListener>()
            currentPlatform.registerInternalListener(ChannelListener())
        }
    }

    fun setupChannels() {
        // Clean up channels and unregister commands
        channelsByName.clear()
        channelsByChatPrefix.clear()
        registeredCommands.forEach { it.unregister(server.commandMap) }

        // Store player toggled channels
        val playerToggledChannels = HashMap<CommandSender, ChannelCommandInfo>()
        server.onlinePlayers.map { BukkitPlatformSender(it) }
                .filter { it.currentChannel != null }
                .forEach {
            playerToggledChannels[it.base as CommandSender] = it.currentChannel!!
        }

        // Register new channels and commands
        eu.mikroskeem.adminchatter.common.platform.config.channels.forEach { channel ->
            if(!channel.isValid()) {
                logger.warning("Channel $channel is invalid! Skipping")
                return@forEach
            }

            channelsByName[channel.channelName] = channel
            if(channel.messagePrefix.isNotEmpty()) {
                channelsByChatPrefix[channel.messagePrefix] = channel
            }

            // Register commands
            val chatCommand = AdminchatterChatCommand(channel).apply { registeredCommands.add(this) }
            val toggleCommand = AdminchatterToggleCommand(channel).apply { registeredCommands.add(this) }
            server.commandMap.register(name.toLowerCase(Locale.ENGLISH), chatCommand)
            server.commandMap.register(name.toLowerCase(Locale.ENGLISH), toggleCommand)
        }

        // Populate toggle channel list again for players
        playerToggledChannels.forEach { _sender, oldChannel ->
            val sender = BukkitPlatformSender(_sender)
            val channel = channelsByName[oldChannel.channelName] ?: run {
                sender.passMessage(currentPlatform.config.messages.toggledChannelDoesNotExistAnymore, oldChannel)
                sender.passMessage(currentPlatform.config.messages.toggledOff, oldChannel)
                sender.currentChannel = null
                return@forEach
            }

            adminchatTogglePlayers[sender.base] = channel
        }

        // Send updated commands list to online players
        server.onlinePlayers.forEach {
            it.updateCommands()
        }
    }

    private fun processMessage(channel: String, player: Player, data: ByteArray) {
        if(channel != PLUGIN_CHANNEL_SOUND)
            return

        player.playSound(String(data))
    }

    val isBehindProxy: Boolean by lazy {
        try {
            if (server.spigot().paperConfig.getBoolean("settings.velocity-support.enabled", false)) {
                return@lazy true
            }
        } catch (e: Throwable) {}

        server.spigot().config.getBoolean("settings.bungeecord", false)
    }
}