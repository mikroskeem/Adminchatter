/*
 * This file is part of project Adminchatter, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2018-2022 Mark Vainomaa <mikroskeem@mikroskeem.eu>
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

package eu.mikroskeem.adminchatter.common

import com.google.common.eventbus.EventBus
import eu.mikroskeem.adminchatter.common.config.ChannelCommandInfo
import java.util.Collections
import java.util.WeakHashMap

/**
 * @author Mark Vainomaa
 */
val eventBus = EventBus()
val channelsByName = Collections.synchronizedMap(HashMap<String, ChannelCommandInfo>())
val channelsByChatPrefix = Collections.synchronizedMap(HashMap<String, ChannelCommandInfo>())
val adminchatTogglePlayers = Collections.synchronizedMap(WeakHashMap<Any, ChannelCommandInfo>())