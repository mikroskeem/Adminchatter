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

import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import org.bstats.bungeecord.Metrics
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference
import java.nio.file.Paths

/**
 * Adminchatter plugin
 *
 * @author Mark Vainomaa
 */
class AdminchatterPlugin: Plugin() {
    lateinit var configLoader: ConfigurationLoader<Adminchatter>
        private set

    private var acCommand: AdminchatCommand? = null
    private var actCommand: AdminchatToggleCommand? = null

    override fun onEnable() {
        configLoader = ConfigurationLoader(
                Paths.get(dataFolder.absolutePath, "config.cfg"),
                Adminchatter::class.java,
                header = CONFIGURATION_FILE_HEADER
        )

        try {
            injectBetterUrlPattern()
        } catch (e: Exception) {
            logger.warning("Failed to inject improved URL regex into TextComponent class. URLs with " +
                    "extremely short domain names may not work!")
            e.printStackTrace()
        }

        setupCommands()
        registerCommand(AdminchatterCommand::class)
        registerListener(ChatListener::class)

        proxy.scheduler.runAsync(this) {
            Metrics(this)
        }
    }

    fun setupCommands() {
        acCommand?.run(proxy.pluginManager::unregisterCommand)
        actCommand?.run(proxy.pluginManager::unregisterCommand)
        config.commands.run {
            acCommand = registerCommand(AdminchatCommand(adminchatCommandName
                    .takeUnless {it.isEmpty() } ?: "adminchat",
                    adminchatCommandAliases.toTypedArray()
            ))
            actCommand = registerCommand(AdminchatToggleCommand(adminchatToggleCommandName
                    .takeUnless { it.isEmpty() } ?: "adminchattoggle",
                    adminchatToggleCommandAliases.toTypedArray()
            ))
        }
    }
}

class ChatListener: Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: ChatEvent) {
        val prefix = config.adminChatMessagePrefix.takeUnless { it.isEmpty() }
        var hadPrefix = false
        var message = event.message

        if(event.isCancelled)
            return

        if(event.isCommand)
            return

        if(event.sender !is ProxiedPlayer)
            return

        if(prefix != null && message != prefix && message.startsWith(prefix)) {
            message = message.substring(prefix.length)
            hadPrefix = true
        }

        if(!hadPrefix && !adminchatTogglePlayers.contains(event.sender as ProxiedPlayer))
            return

        event.isCancelled = true
        sendAdminChat(event.sender as ProxiedPlayer, message)
    }
}