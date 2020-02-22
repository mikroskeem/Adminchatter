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

import org.bukkit.Sound
import org.bukkit.command.CommandExecutor
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Mark Vainomaa
 */
val plugin: AdminchatterPlugin get() = JavaPlugin.getPlugin(AdminchatterPlugin::class.java)

inline fun <reified T: CommandExecutor> JavaPlugin.registerCommand(name: String) {
    val command = T::class.java.getConstructor().newInstance()
    getCommand(name)!!.apply {
        setExecutor(command)
        if(command is TabCompleter)
            tabCompleter = command
    }
}

inline fun <reified T: Listener> JavaPlugin.registerListener() {
    server.pluginManager.registerEvents(T::class.java.getConstructor().newInstance(), this)
}

fun Player.playSound(soundString: String) {
    val (sound, volume, pitch) = soundString.split(":", limit = 3).takeIf { it.size == 3 } ?: run {
        plugin.logger.warning("Invalid sound string: $soundString")
        return
    }

    playSound(location,
            Sound.values().firstOrNull { it.name == sound } ?: run { plugin.logger.warning("Invalid sound: $sound"); return },
            volume.toFloatOrNull() ?: run { plugin.logger.warning("Invalid volume: $volume"); return },
            pitch.toFloatOrNull() ?: run { plugin.logger.warning("Invalid pitch: $pitch"); return }
    )
}