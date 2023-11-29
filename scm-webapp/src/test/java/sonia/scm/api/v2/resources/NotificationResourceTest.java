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

package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.notifications.NotificationStore;
import sonia.scm.notifications.StoredNotification;
import sonia.scm.notifications.Type;
import sonia.scm.sse.ChannelRegistry;
import sonia.scm.web.RestDispatcher;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationResourceTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Mock
  private NotificationStore store;

  @Mock
  private ChannelRegistry channelRegistry;

  private RestDispatcher dispatcher;

  @BeforeEach
  void setUp() {
    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(
      new TestingRootResource(
        new NotificationResource(store, channelRegistry)
      )
    );
  }

  @Test
  void shouldReturnAllNotifications() throws IOException, URISyntaxException {
    notifications("One", "Two", "Three");

    MockHttpRequest request = MockHttpRequest.get("/api/v2/notifications");
    MockHttpResponse response = invoke(request);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    JsonNode node = mapper.readTree(response.getContentAsString());
    JsonNode notificationNodes = node.get("_embedded").get("notifications");
    assertThat(notificationNodes.get(0).get("message").asText()).isEqualTo("One");
    assertThat(notificationNodes.get(1).get("message").asText()).isEqualTo("Two");
    assertThat(notificationNodes.get(2).get("message").asText()).isEqualTo("Three");
  }

  @Test
  void shouldReturnDismissLink() throws IOException, URISyntaxException {
    String id = notifications("One").get(0).getId();

    MockHttpRequest request = MockHttpRequest.get("/api/v2/notifications");
    MockHttpResponse response = invoke(request);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);

    JsonNode node = mapper.readTree(response.getContentAsString());
    String dismissHref = node.get("_embedded")
      .get("notifications")
      .get(0)
      .get("_links")
      .get("dismiss")
      .get("href")
      .asText();

    assertThat(dismissHref).isEqualTo("/api/v2/notifications/" + id);
  }

  @Test
  void shouldReturnCollectionLinks() throws IOException, URISyntaxException {
    notifications();

    MockHttpRequest request = MockHttpRequest.get("/api/v2/notifications");
    MockHttpResponse response = invoke(request);

    JsonNode node = mapper.readTree(response.getContentAsString());
    JsonNode links = node.get("_links");

    assertThat(links.get("self").get("href").asText()).isEqualTo("/api/v2/notifications");
    assertThat(links.get("clear").get("href").asText()).isEqualTo("/api/v2/notifications");
  }

  @Test
  void shouldRemoveNotification() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.delete("/api/v2/notifications/abc42");
    MockHttpResponse response = invoke(request);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);

    verify(store).remove("abc42");
  }

  @Test
  void shouldClear() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.delete("/api/v2/notifications");
    MockHttpResponse response = invoke(request);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    verify(store).clear();
  }

  private MockHttpResponse invoke(MockHttpRequest request) {
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  private List<StoredNotification> notifications(String... messages) {
    List<StoredNotification> notifications = Arrays.stream(messages)
      .map(this::notification)
      .collect(Collectors.toList());

    when(store.getAll()).thenReturn(notifications);
    return notifications;
  }

  private StoredNotification notification(String m) {
    return new StoredNotification(UUID.randomUUID().toString(), Type.INFO, "/notify", m, emptyMap(), Instant.now());
  }

  @Path("/api/v2")
  public static class TestingRootResource {

    private final NotificationResource resource;

    public TestingRootResource(NotificationResource resource) {
      this.resource = resource;
    }

    @Path("notifications")
    public NotificationResource notifications() {
      return resource;
    }
  }

}
