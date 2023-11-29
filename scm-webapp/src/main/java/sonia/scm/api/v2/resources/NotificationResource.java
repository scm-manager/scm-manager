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
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import sonia.scm.notifications.NotificationChannelId;
import sonia.scm.notifications.NotificationStore;
import sonia.scm.notifications.StoredNotification;
import sonia.scm.security.SessionId;
import sonia.scm.sse.Channel;
import sonia.scm.sse.ChannelRegistry;
import sonia.scm.sse.Registration;
import sonia.scm.sse.SseResponse;
import sonia.scm.web.VndMediaType;

import java.util.List;
import java.util.stream.Collectors;

@OpenAPIDefinition(tags = {
  @Tag(name = "Notifications", description = "Notification related endpoints")
})
@Path(NotificationResource.NOTIFICATION_PATH_V2)
public class NotificationResource {
  static final String NOTIFICATION_PATH_V2 = "v2/me/notifications/";

  private final NotificationStore store;
  private final ChannelRegistry channelRegistry;

  @Inject
  public NotificationResource(NotificationStore store, ChannelRegistry channelRegistry) {
    this.store = store;
    this.channelRegistry = channelRegistry;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.NOTIFICATION_COLLECTION)
  @Operation(
    summary = "Notifications",
    description = "Returns all notifications for the current user",
    tags = "Notifications",
    operationId = "notifications_get_all"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.NOTIFICATION_COLLECTION,
      schema = @Schema(implementation = HalRepresentation.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public HalRepresentation getAll(@Context UriInfo uriInfo) {
    return new HalRepresentation(
      createCollectionLinks(uriInfo),
      createEmbeddedNotifications(uriInfo)
    );
  }

  @DELETE
  @Path("{id}")
  @Operation(
    summary = "Dismiss",
    description = "Dismiss the notification with the given id",
    tags = "Notifications",
    operationId = "notifications_dismiss"
  )
  @ApiResponse(
    responseCode = "204",
    description = "no content"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response dismiss(@PathParam("id") String id) {
    store.remove(id);
    return Response.noContent().build();
  }

  @DELETE
  @Path("")
  @Operation(
    summary = "Dismiss all",
    description = "Dismiss all notifications for the current user",
    tags = "Notifications",
    operationId = "notifications_dismiss_all"
  )
  @ApiResponse(
    responseCode = "204",
    description = "no content"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response dismissAll() {
    store.clear();
    return Response.noContent().build();
  }

  @GET
  @Path("subscribe")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @Operation(
    summary = "Subscribe",
    description = "Subscribe to the sse event stream of notification for the current user",
    tags = "Notifications",
    operationId = "notifications_subscribe"
  )
  @ApiResponse(
    responseCode = "200"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @SseResponse
  public void subscribe(@Context Sse sse, @Context SseEventSink eventSink, @QueryParam(SessionId.PARAMETER) SessionId sessionId) {
    Channel channel = channelRegistry.channel(NotificationChannelId.current());
    channel.register(new Registration(sessionId, sse, eventSink));
  }

  private Embedded createEmbeddedNotifications(UriInfo uriInfo) {
    List<NotificationDto> notifications = store.getAll()
      .stream()
      .map(n -> map(uriInfo, n))
      .collect(Collectors.toList());
    return Embedded.embedded("notifications", notifications);
  }

  private NotificationDto map(UriInfo uriInfo, StoredNotification storedNotification) {
    String href = uriInfo.getAbsolutePathBuilder().path(storedNotification.getId()).build().toASCIIString();
    Links links = Links.linkingTo().single(Link.link("dismiss", href)).build();
    return new NotificationDto(storedNotification, links);
  }

  private Links createCollectionLinks(UriInfo uriInfo) {
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
