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

package eu.mikroskeem.adminchatter.common.config

import com.google.common.reflect.TypeToken
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer

/**
 * @author Mark Vainomaa
 */
data class ChannelCommandInfo(
        /** Channel name */
        val channelName: String,

        /** Pretty channel name (mainly used in placeholders) */
        val prettyChannelName: String,

        /** Channel command name */
        val commandName: String,

        /** Message prefix to send message to given channel */
        val messagePrefix: String,

        /** Channel toggle command name */
        val toggleCommandName: String,

        /** Channel command aliases */
        val commandAliases: List<String>,

        /** Channel toggle command aliases */
        val toggleCommandAliases: List<String>,

        /** Channel message format */
        val messageFormat: String,

        /** Channel message hover text */
        val messageHoverText: String,

        /** Channel message click command */
        val clickCommand: String,

        /** Channel sound effect */
        val soundEffect: String
) {
    fun isValid(): Boolean {
        // Channel name must not contain spaces
        if(channelName.isEmpty() || channelName.contains(' ')) {
            return false
        }

        // Command names must not contain spaces
        if(commandName.isEmpty() || toggleCommandName.isEmpty() || commandName.contains(' ') || toggleCommandName.contains(' ')) {
            return false
        }

        // Ditto for aliases
        if(commandAliases.any { it.isEmpty() || it.contains(' ') } || toggleCommandAliases.any { it.isEmpty() || it.contains(' ') }) {
            return false
        }

        return true
    }

    companion object ChannelCommandInfoSerializer: TypeSerializer<ChannelCommandInfo> {
        private val STRING_TOKEN = object: TypeToken<String>() {}

        private const val PREFIX_COMMENT = "If chat message starts with given prefix, then it will be " +
                "passed to adminchat directly (however chat message sender needs to have `adminchatter.chat`" +
                "permission for that to happen. Set empty to disable this feature"

        private val SOUND_COMMENT = """
        What sound should be played when player receives an adminchat message?
        Set empty to disable
        Requires Adminchatter plugin to be installed on all game servers
        Format: [Sound name from Bukkit sound enum : Volume : Pitch]
        See https://papermc.io/javadocs/paper/1.13/org/bukkit/Sound.html
        """.trimIndent()

        // TODO: This is really really error prone
        override fun deserialize(type: TypeToken<*>, node: ConfigurationNode): ChannelCommandInfo {
            val name = node.getNode("name").string!!
            val prettyName = node.getNode("pretty-name").string!!
            val commandName = node.getNode("command-name").string!!
            val prefix = node.getNode("message-prefix").string!!
            val toggleName = node.getNode("toggle-command-name").string!!
            val aliases = node.getNode("command-aliases").getList(STRING_TOKEN)
            val toggleAliases = node.getNode("toggle-command-aliases").getList(STRING_TOKEN)
            val messageFormat = node.getNode("format").string!!
            val messageHoverText = node.getNode("hover-text").string!!
            val command = node.getNode("click-command").string!!
            val sound = node.getNode("sound").string!!

            return ChannelCommandInfo(name, prettyName, commandName,
                    prefix, toggleName, aliases,
                    toggleAliases, messageFormat, messageHoverText,
                    command, sound)
        }

        override fun serialize(type: TypeToken<*>, instance: ChannelCommandInfo?, node: ConfigurationNode) {
            instance!!.run {
                node.getNode("name").run {
                    (this as? CommentedConfigurationNode)?.setComment("What should channel name be? Must not be empty or contain spaces")

                    value = channelName
                }

                node.getNode("pretty-name").run {
                    (this as? CommentedConfigurationNode)?.setComment("What should channel pretty name be? It is used only in placeholders of plugin messages. Must not be empty")

                    value = prettyChannelName
                }

                node.getNode("command-name").run {
                    (this as? CommentedConfigurationNode)?.setComment("What should be channel command name be (without slash)? Must not be empty or contain spaces")

                    value = commandName
                }

                node.getNode("message-prefix").run {
                    (this as? CommentedConfigurationNode)?.setComment(PREFIX_COMMENT)

                    value = messagePrefix
                }

                node.getNode("toggle-command-name").run {
                    (this as? CommentedConfigurationNode)?.setComment("What should be channel toggle command name be (without slash)? Must not be empty or contain spaces")

                    value = toggleCommandName
                }

                node.getNode("command-aliases").run {
                    (this as? CommentedConfigurationNode)?.setComment("What should be channel command aliases be (without slashes)? Commands names must not be empty or contain spaces, however aliases can be omitted.")

                    value = commandAliases
                }

                node.getNode("toggle-command-aliases").run {
                    (this as? CommentedConfigurationNode)?.setComment("What should be channel toggle command aliases be (without slashes)? Commands names must not be empty or contain spaces, however aliases can be omitted.")

                    value = toggleCommandAliases
                }

                node.getNode("format").run {
                    (this as? CommentedConfigurationNode)?.setComment("Format how channel chat message should look like. Should not be empty (well, what's the point of this plugin then?)")

                    value = messageFormat
                }

                node.getNode("hover-text").run {
                    (this as? CommentedConfigurationNode)?.setComment("Text which is displayed when player hovers over admin chat message\nSet empty to disable")

                    value = messageHoverText
                }

                node.getNode("click-command").run {
                    (this as? CommentedConfigurationNode)?.setComment("Command which is run when player clicks on admin chat message\nSet empty to disable")

                    value = clickCommand
                }

                node.getNode("sound").run {
                    (this as? CommentedConfigurationNode)?.setComment(SOUND_COMMENT)

                    value = soundEffect
                }
            }
        }
    }
}