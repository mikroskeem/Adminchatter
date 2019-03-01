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

package eu.mikroskeem.adminchatter

import com.google.common.reflect.TypeToken
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer

/**
 * @author Mark Vainomaa
 */
@ConfigSerializable
class Adminchatter {
    @Setting(value = "allow-console-usage", comment = "Whether to allow console usage for adminchat or not")
    var allowConsoleUsage = false
        private set

    @Setting(value = "console-user-name", comment = "When console tries to use adminchat, what name should one have?")
    var consoleName = "CONSOLE"
        private set

    @Setting(value = "none-server-name", comment = "What server name to use when server info is not accessible (e.g " +
            "console admin chat usage)")
    var noneServerName = "none"
        private set

    @Setting(value = "pretty-server-names", comment = "Pretty server names. Used with `{pretty_server_name}` placeholder. " +
            "Defaults to server default name if one is not present here.`")
    var prettyServerNames: Map<String, String> = mapOf(
            Pair("lobby", "&eLobby")
    )
        private set

    @Setting(value = "messages", comment = "Plugin messages")
    var messages = Messages()
        private set

    @Setting(value = "channels", comment = "What channels does this plugin support?")
    var channels = listOf(
            ChannelCommandInfo(
                    "admin",
                    "&cAdmin",
                    "adminchat",
                    "@",
                    "adminchattoggle",
                    listOf("ac"),
                    listOf("actoggle", "act"),
                    "&8&l[&cAC&8&l] &f{player_name} &0» &c{message}",
                    "&cServer &8» &a{pretty_server_name}",
                    "/server {server_name}",
                    "ENTITY_ITEM_PICKUP:0.5:1"
            ),
            ChannelCommandInfo(
                    "mod",
                    "&bMod",
                    "modchat",
                    "#",
                    "modchattoggle",
                    listOf("mc"),
                    listOf("mctoggle", "mct"),
                    "&8&l[&bMC&8&l] &f{player_name} &0» &b{message}",
                    "&cServer &8» &a{pretty_server_name}",
                    "/server {server_name}",
                    "ENTITY_RABBIT_HURT:0.5:1"
            )
    )
        private set
}

@ConfigSerializable
class Messages {
    @Setting(value = "plugin-prefix", comment = "Plugin message prefix")
    var messagePrefix = "&8&l[&cAdminchatter&8&l] »"
        private set

    @Setting(value = "toggled-on", comment = "This message is shown when player toggles channel chat on")
    var toggledOn = "{plugin_prefix} &7Chatting in channel {channel_name}&7 is now toggled &aon &7for you."
        private set

    @Setting(value = "toggled-off", comment = "This message is shown when player toggles channel chat off")
    var toggledOff = "{plugin_prefix} &7Chatting in channel {channel_name}&7 is now toggled &coff &7for you."
        private set

    @Setting(value = "channel-switched", comment = "This message is shown when player toggles channel chat off")
    var channelSwitched = "{plugin_prefix} &7You have toggled chatting into channel {channel_name}&7 now."
        private set

    @Setting(value = "toggling-is-only-for-players", comment = "This message is shown when non-player is trying " +
            "to use channel toggle feature")
    var togglingIsOnlyForPlayers = "{plugin_prefix} &cChat channel toggling is only supported for players"
        private set

    @Setting(value = "channel-chat-is-only-for-players", comment = "This message is shown when non-player is trying " +
            "to use channel chat feature, while it is disabled for console")
    var channelChatIsOnlyForPlayers = "{plugin_prefix} &cChat channels for console are turned off from configuration"
        private set

    @Setting(value = "plugin-configuration-is-reloaded", comment = "This message is shown when plugin configuration reloading succeeded")
    var pluginConfigurationReloaded = "{plugin_prefix} &aPlugin configuration is reloaded!"
        private set

    @Setting(value = "must-supply-a-message", comment = "This message is shown when user tries to send empty adminchat message using command")
    var mustSupplyAMessage = "{plugin_prefix} &cYou must supply a message!"
        private set

    @Setting(value = "url-component-hover-text", comment = "This text will be used in text components which contain a clickable url. " +
            "Set empty to disable this feature and use same hover event as surrounding text does.")
    var urlHoverText = "&eClick to open an URL"
        private set
}

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
    companion object ChannelCommandInfoSerializer: TypeSerializer<ChannelCommandInfo> {
        private val STRING_TOKEN = object: TypeToken<String>() {}

        private const val PREFIX_COMMENT = "If chat message starts with given prefix, then it will be " +
                "passed to adminchat directly (however chat message sender needs to have `adminchatter.chat`" +
                "permission for that to happen"

        private val SOUND_COMMENT = """
        What sound should be played when player receives an adminchat message?
        Set empty to disable
        Requires Adminchatter plugin to be installed on all game servers
        Format: [Sound name from Bukkit sound enum : Volume : Pitch]
        See https://ci.destroystokyo.com/userContent/apidocs/org/bukkit/Sound.html
        """.trimIndent()

        // TODO: This is really really error prone
        override fun deserialize(type: TypeToken<*>, node: ConfigurationNode): ChannelCommandInfo {
            val name = node.getNode("name").string!!
            val prettyName = node.getNode("pretty-name").string!!
            val commandName = node.getNode("command-name").string!!
            val prefix = node.getNode("message-prefix").string!!
            val toggleName = node.getNode("toggle-command-name").string!!
            val aliases = node.getNode("command-aliases").getList(STRING_TOKEN)!!
            val toggleAliases = node.getNode("toggle-command-aliases").getList(STRING_TOKEN)!!
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
                    (this as? CommentedConfigurationNode)?.setComment("What should channel name be?")

                    value = channelName
                }

                node.getNode("pretty-name").run {
                    (this as? CommentedConfigurationNode)?.setComment("What should channel pretty name be? It is used only in placeholders of plugin messages")

                    value = prettyChannelName
                }

                node.getNode("command-name").run {
                    (this as? CommentedConfigurationNode)?.setComment("What should be channel command name be (without slash)?")

                    value = commandName
                }

                node.getNode("message-prefix").run {
                    (this as? CommentedConfigurationNode)?.setComment(PREFIX_COMMENT)

                    value = messagePrefix
                }

                node.getNode("toggle-command-name").run {
                    (this as? CommentedConfigurationNode)?.setComment("What should be channel toggle command name be (without slash)?")

                    value = toggleCommandName
                }

                node.getNode("command-aliases").run {
                    (this as? CommentedConfigurationNode)?.setComment("What should be channel command aliases be (without slashes)?")

                    value = commandAliases
                }

                node.getNode("toggle-command-aliases").run {
                    (this as? CommentedConfigurationNode)?.setComment("What should be channel toggle command aliases be (without slashes)?")

                    value = toggleCommandAliases
                }

                node.getNode("format").run {
                    (this as? CommentedConfigurationNode)?.setComment("Format how channel chat message should look like.")

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