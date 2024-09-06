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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginPermissions;
import sonia.scm.web.VndMediaType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class AvailablePluginResource {

  private final PluginDtoCollectionMapper collectionMapper;
  private final PluginManager pluginManager;
  private final PluginDtoMapper mapper;

  @Inject
  public AvailablePluginResource(PluginDtoCollectionMapper collectionMapper, PluginManager pluginManager, PluginDtoMapper mapper) {
    this.collectionMapper = collectionMapper;
    this.pluginManager = pluginManager;
    this.mapper = mapper;
  }

  /**
   * Returns a collection of available plugins.
   */
  @GET
  @Path("")
  @Operation(
    summary = "Find all available plugins",
    description = "Returns a collection of available plugins.",
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
  @Produces(VndMediaType.PLUGIN_COLLECTION)
  public Response getAvailablePlugins() {
    PluginPermissions.read().check();
    PluginManager.PluginResult plugins = pluginManager.getPlugins();
    List<AvailablePlugin> available = plugins.getAvailablePlugins().stream().filter(a -> notInstalled(a, plugins.getInstalledPlugins())).collect(Collectors.toList());

    return Response.ok(collectionMapper.mapAvailable(available, plugins.getPluginCenterStatus())).build();
  }

  private boolean notInstalled(AvailablePlugin a, List<InstalledPlugin> installed) {
    return installed.stream().noneMatch(installedPlugin -> installedPlugin.getDescriptor().getInformation().getName().equals(a.getDescriptor().getInformation().getName()));
  }

  /**
   * Returns available plugin.
   */
  @GET
  @Path("/{name}")
  @Produces(VndMediaType.PLUGIN)
  @Operation(
    summary = "Find single available plugin",
    description = "Returns an available plugins.",
    tags = "Plugin Management"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.PLUGIN,
      schema = @Schema(implementation = PluginDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"plugin:read\" privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no plugin with the specified name found",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response getAvailablePlugin(@PathParam("name") String name) {
    PluginPermissions.read().check();
    Optional<AvailablePlugin> plugin = pluginManager.getAvailable(name);
    if (plugin.isPresent()) {
      return Response.ok(mapper.mapAvailable(plugin.get())).build();
    } else {
      throw notFound(entity("Plugin", name));
    }
  }

  /**
   * Triggers plugin installation.
   *
   * @param name plugin name
   * @return HTTP Status.
   */
  @POST
  @Path("/{name}/install")
  @Operation(
    summary = "Triggers plugin installation",
    description = "Put single plugin in installation queue. Plugin will be installed after restart.",
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
  public Response installPlugin(@PathParam("name") String name, @QueryParam("restart") boolean restartAfterInstallation) {
    PluginPermissions.write().check();
    pluginManager.install(name, restartAfterInstallation);
    return Response.ok().build();
  }
}
