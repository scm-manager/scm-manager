/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.sse;

import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Singleton
public class ChannelRegistry {

  private final Map<Object, Channel> channels = new ConcurrentHashMap<>();
  private final Function<Object, Channel> channelFactory;

  public ChannelRegistry() {
    this(Channel::new);
  }

  @VisibleForTesting
  ChannelRegistry(Function<Object, Channel> channelFactory) {
    this.channelFactory = channelFactory;
  }

  public Channel channel(Object channelId) {
    return channels.computeIfAbsent(channelId, channelFactory);
  }

  void removeClosedClients() {
    channels.values().forEach(Channel::removeClosedOrTimeoutClients);
  }
}
