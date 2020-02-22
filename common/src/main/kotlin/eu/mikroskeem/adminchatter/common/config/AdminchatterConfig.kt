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

import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable

/**
 * @author Mark Vainomaa
 */
const val CONFIGURATION_FILE_HEADER = """
 An awesome adminchat plugin, made with ♥ by mikroskeem

 Globally supported placeholders:
 - {plugin_prefix} -> plugin message prefix

 Placeholders, which are only available on adminchat messages:
 - {player_name} -> pretty obvious
 - {message} -> pretty obvious again
 - {colored_message} -> message, now just with fancy colors what players may add
 - {server_name} -> server where given player sent the adminchat message. For console, 'none' is used
 - {channel_name} -> Chat channel name. See channels section
 - {pretty_server_name} -> see above, just server name from configuration option `pretty-server-names` is used instead
"""

@ConfigSerializable
class AdminchatterConfig {
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