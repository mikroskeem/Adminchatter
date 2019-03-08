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

import org.bstats.bukkit.Metrics
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Mark Vainomaa
 */
class AdminchatterPluginBukkit: JavaPlugin() {
    override fun onEnable() {
        server.scheduler.runTaskAsynchronously(this, Runnable {
            Metrics(this)
        })

        server.messenger.registerIncomingPluginChannel(this, "Adminchatter", this::processMessage)
    }

    private fun processMessage(channel: String, player: Player, data: ByteArray) {
        if(channel != "Adminchatter")
            return

        player.playSound(String(data))
    }
}

private fun Player.playSound(soundData: String) {
    val (sound, volume, pitch) = soundData.split(":", limit = 3).takeIf { it.size == 3 }
            ?: return

    playSound(player.location,
            Sound.values().firstOrNull { it.name == sound } ?: return,
            volume.toFloatOrNull() ?: return,
            pitch.toFloatOrNull() ?: return
    )
}