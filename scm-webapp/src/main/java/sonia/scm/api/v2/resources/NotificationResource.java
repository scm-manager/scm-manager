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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import sonia.scm.notifications.NotificationStore;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationResource {

  private final NotificationStore store;

  @Inject
  public NotificationResource(NotificationStore store) {
    this.store = store;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.NOTIFICATION)
  public HalRepresentation getAll(@Context UriInfo uriInfo) {
    return new HalRepresentation(
      createNotificationLinks(uriInfo),
      createEmbeddedNotifications()
    );
  }

  @DELETE
  @Path("")
  public Response clear() {
    store.clear();
    return Response.noContent().build();
  }

  @GET
  @Path("subscribe")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  public Response subscribe(@Context Sse sse, @Context SseEventSink eventSink) {
    return Response.noContent().build();
  }

  private Embedded createEmbeddedNotifications() {
    List<NotificationDto> notifications = store.getAll()
      .stream()
      .map(NotificationDto::new)
      .collect(Collectors.toList());
    return Embedded.embedded("notifications", notifications);
  }

  private Links createNotificationLinks(UriInfo uriInfo) {
    String self = selfLink(uriInfo);
    return Links.linkingTo()
      .self(self)
      .single(Link.link("clear", self))
      .single(Link.link("subscribe", subscribeLink(uriInfo)))
      .build();
  }

  private String selfLink(UriInfo uriInfo) {
    return uriInfo.getAbsolutePath().toASCIIString();
  }

  private String subscribeLink(UriInfo uriInfo) {
    return uriInfo.getRequestUriBuilder().path("subscribe").build().toASCIIString();
  }
}
