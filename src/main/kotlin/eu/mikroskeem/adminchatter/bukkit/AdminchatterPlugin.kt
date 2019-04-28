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

package eu.mikroskeem.adminchatter.bukkit

import com.google.common.reflect.TypeToken
import eu.mikroskeem.adminchatter.common.ConfigurationLoader
import eu.mikroskeem.adminchatter.common.channelsByChatPrefix
import eu.mikroskeem.adminchatter.common.channelsByName
import eu.mikroskeem.adminchatter.common.config.AdminchatterConfig
import eu.mikroskeem.adminchatter.common.config.CONFIGURATION_FILE_HEADER
import eu.mikroskeem.adminchatter.common.config.ChannelCommandInfo
import eu.mikroskeem.adminchatter.common.platform.BukkitPlatform
import eu.mikroskeem.adminchatter.common.platform.currentPlatform
import eu.mikroskeem.adminchatter.common.utils.BASE_CHAT_PERMISSION
import eu.mikroskeem.adminchatter.common.utils.PLUGIN_CHANNEL_SOUND
import eu.mikroskeem.adminchatter.common.utils.injectBetterUrlPattern
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers
import org.bstats.bukkit.MetricsLite
import org.bukkit.command.Command
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

        if(server.spigot().spigotConfig.getBoolean("settings.bungeecord", false)) {
            logger.info("BungeeCord mode - listening for sound notification messages")
            server.messenger.registerIncomingPluginChannel(this, PLUGIN_CHANNEL_SOUND, this::processMessage)
            return
        }

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

        registerListener<ChatListener>()
        registerCommand<AdminchatterCommand>("adminchatter")
        setupChannels()
    }

    fun setupChannels() {
        // Clean up channels and unregister commands
        channelsByName.clear()
        channelsByChatPrefix.clear()
        registeredCommands.forEach { it.unregister(server.commandMap) }

        // Register new channels and commands
        eu.mikroskeem.adminchatter.common.platform.config.channels.forEach { channel ->
            // TODO: validate channel info here

            channelsByName[channel.channelName] = channel
            channelsByChatPrefix[channel.messagePrefix] = channel

            // Register commands
            val chatCommand = AdminchatterChatCommand(channel).apply { registeredCommands.add(this) }
            val toggleCommand = AdminchatterToggleCommand(channel).apply { registeredCommands.add(this) }
            server.commandMap.register(name.toLowerCase(Locale.ENGLISH), chatCommand)
            server.commandMap.register(name.toLowerCase(Locale.ENGLISH), toggleCommand)
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

    companion object {
        init {
            TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(ChannelCommandInfo::class.java), ChannelCommandInfo)
        }
    }
}