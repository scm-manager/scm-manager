/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
