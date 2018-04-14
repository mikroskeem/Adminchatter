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

import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable

/**
 * @author Mark Vainomaa
 */
@ConfigSerializable
class Adminchatter {
    @Setting(value = "admin-chat-format", comment = "Format how admin chat message should look like.")
    var adminChatFormat = "&8&l[&cAC&8&l] &f{player_name} &0» &c{message}"
        private set

    @Setting(value = "admin-chat-hover-text", comment = "Text which is displayed when player hovers over admin chat message\n" +
            "Set it empty to disable")
    var adminChatHoverText = "&cServer &8» &a{pretty_server_name}"
        private set

    @Setting(value = "admin-chat-click-command", comment = "Command which is run when player clicks on admin chat message\n" +
            "Set it to empty to disable")
    var adminChatClickCommand = "/server {server_name}"
        private set

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

    @Setting(value = "admin-chat-message-prefix", comment = "If chat message starts with given prefix, then it will " +
            "be passed to adminchat directly (however chat message sender needs to have `adminchatter.chat` permission " +
            "for that to happen)")
    var adminChatMessagePrefix = "@"
        private set

    @Setting(value = "messages", comment = "Plugin messages")
    var messages = Messages()
        private set

    @Setting(value = "commands", comment = "Plugin commands")
    var commands = Commands()
        private set

    @Setting(value = "sound", comment = "What sound should be played when player receives an adminchat message? " +
            "Set empty to disable\n" +
            "Requires Adminchatter plugin to be installed on all game servers\n" +
            "Format: [Sound name from Bukkit sound enum : Volume : Pitch]\n" +
            "See https://ci.destroystokyo.com/userContent/apidocs/org/bukkit/Sound.html")
    var sound = "ENTITY_ITEM_PICKUP:0.5:1"
        private set
}

@ConfigSerializable
class Messages {
    @Setting(value = "plugin-prefix", comment = "Plugin message prefix")
    var messagePrefix = "&8&l[&cAdminchatter&8&l] »"
        private set

    @Setting(value = "admin-chat-toggle-enabled", comment = "This message is shown when player toggles admin chat on")
    var adminChatToggleEnabled = "{plugin_prefix} &7Adminchat is now toggled &aon &7for you."
        private set

    @Setting(value = "admin-chat-toggle-disabled", comment = "This message is shown when player toggles admin chat off")
    var adminChatToggleDisabled = "{plugin_prefix} &7Adminchat is now toggled &coff &7for you."
        private set

    @Setting(value = "admin-chat-toggling-is-only-for-players", comment = "This message is shown when non-player is trying " +
            "to use adminchat toggle feature")
    var adminChatTogglingIsOnlyForPlayers = "{plugin_prefix} &cAdmin chat toggling is only supported for players"
        private set

    @Setting(value = "admin-chat-is-only-for-players", comment = "This message is shown when non-player is trying " +
            "to use adminchat feature, while it is disabled for console")
    var adminChatIsOnlyForPlayers = "{plugin_prefix} &cAdmin chat for console is turned off from configuration"
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

@ConfigSerializable
class Commands {
    @Setting(value = "adminchat-command-name", comment = "What should be adminchat command name be (without slash)?")
    var adminchatCommandName = "adminchat"
        private set

    @Setting(value = "adminchat-command-aliases", comment = "What should be adminchat command aliases be (without slashes)?")
    var adminchatCommandAliases = listOf("ac")
        private set

    @Setting(value = "adminchat-toggle-command-name", comment = "What should be adminchat toggle command name be (without slash)?")
    var adminchatToggleCommandName = "adminchattoggle"
        private set

    @Setting(value = "adminchat-toggle-command-aliases", comment = "What should be adminchat toggle command aliases be (without slashes)?")
    var adminchatToggleCommandAliases = listOf("actoggle", "act")
        private set
}