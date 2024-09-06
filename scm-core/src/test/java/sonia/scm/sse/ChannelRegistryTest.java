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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ChannelRegistryTest {

  private ChannelRegistry registry;

  @Nested
  class ChannelTests {

  @BeforeEach
  void setUp() {
    registry = new ChannelRegistry();
  }

  @Test
  void shouldCreateNewChannel() {
    Channel one = registry.channel("one");
    assertThat(one).isNotNull();
  }

  @Test
  void shouldReturnSameChannelForSameId() {
    Channel two = registry.channel("two");
    assertThat(two).isSameAs(registry.channel("two"));
  }

  }

  @Nested
  @ExtendWith(MockitoExtension.class)
  class RemoveClosedClientsTests {

    private List<Channel> channels;

    @BeforeEach
    void setUp() {
      channels = new ArrayList<>();
      registry = new ChannelRegistry(objectId -> {
        Channel channel = mock(Channel.class);
        channels.add(channel);
        return channel;
      });
    }

    @Test
    void shouldCallRemoveClosedOrTimeoutClientsOnEachChannel() {
      registry.channel("one");
      registry.channel("two");
      registry.channel("three");

      registry.removeClosedClients();

      assertThat(channels)
        .hasSize(3)
        .allSatisfy(channel -> verify(channel).removeClosedOrTimeoutClients());
    }

  }

}
