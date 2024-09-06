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

package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import sonia.scm.lifecycle.Restarter;
import sonia.scm.plugin.PendingPlugins;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginPermissions;
import sonia.scm.web.VndMediaType;

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
