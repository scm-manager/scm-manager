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
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginPermissions;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

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
    List<AvailablePlugin> pending = pluginManager
      .getAvailable()
      .stream()
      .filter(AvailablePlugin::isPending)
      .collect(toList());
    List<InstalledPlugin> installed = pluginManager.getInstalled();

    Stream<AvailablePlugin> newPlugins = pending
      .stream()
      .filter(a -> !contains(installed, a));
    Stream<InstalledPlugin> updatePlugins = installed
      .stream()
      .filter(i -> contains(pending, i));
    Stream<InstalledPlugin> uninstallPlugins = installed
      .stream()
      .filter(InstalledPlugin::isMarkedForUninstall);

    Links.Builder linksBuilder = linkingTo().self(resourceLinks.pendingPluginCollection().self());

    List<PluginDto> installDtos = newPlugins.map(mapper::mapAvailable).collect(toList());
    List<PluginDto> updateDtos = updatePlugins.map(i -> mapper.mapInstalled(i, pending)).collect(toList());
    List<PluginDto> uninstallDtos = uninstallPlugins.map(i -> mapper.mapInstalled(i, pending)).collect(toList());

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

  private boolean contains(Collection<InstalledPlugin> installedPlugins, AvailablePlugin availablePlugin) {
    return installedPlugins
      .stream()
      .anyMatch(installedPlugin -> haveSameName(installedPlugin, availablePlugin));
  }

  private boolean contains(Collection<AvailablePlugin> availablePlugins, InstalledPlugin installedPlugin) {
    return availablePlugins
      .stream()
      .anyMatch(availablePlugin -> haveSameName(installedPlugin, availablePlugin));
  }

  private boolean haveSameName(InstalledPlugin installedPlugin, AvailablePlugin availablePlugin) {
    return installedPlugin.getDescriptor().getInformation().getName().equals(availablePlugin.getDescriptor().getInformation().getName());
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
