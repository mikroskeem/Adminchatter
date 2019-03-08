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

package eu.mikroskeem.adminchatter.common.config

import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable

/**
 * @author Mark Vainomaa
 */
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