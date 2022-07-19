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
import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.lifecycle.Restarter;
import sonia.scm.plugin.PendingPlugins;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginPermissions;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.stream.Collectors.toList;

public class PendingPluginResource {

  private final PluginManager pluginManager;
  private final ResourceLinks resourceLinks;
  private final PluginDtoMapper mapper;
  private final Restarter restarter;

  @Inject
  public PendingPluginResource(PluginManager pluginManager, ResourceLinks resourceLinks, PluginDtoMapper mapper, Restarter restarter) {
    this.pluginManager = pluginManager;
    this.resourceLinks = resourceLinks;
    this.mapper = mapper;
    this.restarter = restarter;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.PLUGIN_COLLECTION)
  @Operation(
    summary = "Find all pending plugins",
    description = "Returns a collection of pending plugins.",
    tags = "Plugin Management"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.PLUGIN_COLLECTION,
      schema = @Schema(implementation = CollectionDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"plugin:read\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response getPending() {
    PendingPlugins pending = pluginManager.getPending();

    Links.Builder linksBuilder = linkingTo().self(resourceLinks.pendingPluginCollection().self());

    List<PluginDto> installDtos = pending.getInstall().stream().map(mapper::mapAvailable).collect(toList());
    List<PluginDto> updateDtos = pending.getUpdate().stream().map(p -> mapper.mapInstalled(p, pending.getInstall())).collect(toList());
    List<PluginDto> uninstallDtos = pending.getUninstall().stream().map(i -> mapper.mapInstalled(i, pending.getInstall())).collect(toList());

    if (
      PluginPermissions.write().isPermitted() &&
        (!installDtos.isEmpty() || !updateDtos.isEmpty() || !uninstallDtos.isEmpty())
    ) {
      if (restarter.isSupported()) {
        linksBuilder.single(link("execute", resourceLinks.pendingPluginCollection().executePending()));
      }
      linksBuilder.single(link("cancel", resourceLinks.pendingPluginCollection().cancelPending()));
    }

    Embedded.Builder embedded = Embedded.embeddedBuilder();
    embedded.with("new", installDtos);
    embedded.with("update", updateDtos);
    embedded.with("uninstall", uninstallDtos);

    return Response.ok(new HalRepresentation(linksBuilder.build(), embedded.build())).build();
  }

  @POST
  @Path("/execute")
  @Operation(
    summary = "Execute pending",
    description = "Executes all pending plugin changes. The server will be restarted on this action.",
    tags = "Plugin Management"
  )
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"plugin:write\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response executePending() {
    pluginManager.executePendingAndRestart();
    return Response.ok().build();
  }

  @POST
  @Path("/cancel")
  @Operation(
    summary = "Cancel pending",
    description = "Cancels all pending plugin changes and clear the pending queue.",
    tags = "Plugin Management"
  )
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"plugin:write\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response cancelPending() {
    pluginManager.cancelPending();
    return Response.ok().build();
  }
}
