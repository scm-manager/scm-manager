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
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.web.VndMediaType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllowAnonymousAccess
public class UIPluginResource {

  private final PluginLoader pluginLoader;
  private final UIPluginDtoCollectionMapper collectionMapper;
  private final UIPluginDtoMapper mapper;

  @Inject
  public UIPluginResource(PluginLoader pluginLoader, UIPluginDtoCollectionMapper collectionMapper, UIPluginDtoMapper mapper) {
    this.pluginLoader = pluginLoader;
    this.collectionMapper = collectionMapper;
    this.mapper = mapper;
  }

  /**
   * Returns a collection of installed plugins and their ui bundles.
   */
  @GET
  @Path("")
  @Produces(VndMediaType.UI_PLUGIN_COLLECTION)
  @Operation(summary = "Collection of ui plugin bundles", description = "Returns a collection of installed plugins and their ui bundles.", hidden = true)
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.UI_PLUGIN_COLLECTION,
      schema = @Schema(implementation = CollectionDto.class)
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response getInstalledPlugins() {
    List<InstalledPlugin> plugins = pluginLoader.getInstalledPlugins()
      .stream()
      .filter(this::filter)
      .collect(Collectors.toList());

    return Response.ok(collectionMapper.map(plugins)).build();
  }

  /**
   * Returns the installed plugin with the given id.
   *
   * @param id id of plugin
   *
   */
  @GET
  @Path("{id}")
  @Produces(VndMediaType.UI_PLUGIN)
  @Operation(summary = "Get single ui plugin bundle", description = "Returns the installed plugin with the given id.", hidden = true)
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.UI_PLUGIN,
      schema = @Schema(implementation = UIPluginDto.class)
    )
  )
  @ApiResponse(
    responseCode = "404",
    description = "not found",
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
    ))
  public Response getInstalledPlugin(@PathParam("id") String id) {
    Optional<UIPluginDto> uiPluginDto = pluginLoader.getInstalledPlugins()
      .stream()
      .filter(this::filter)
      .filter(plugin -> id.equals(plugin.getId()))
      .map(mapper::map)
      .findFirst();

    if (uiPluginDto.isPresent()) {
      return Response.ok(uiPluginDto.get()).build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  private boolean filter(InstalledPlugin plugin) {
    return plugin.getDescriptor().getResources() != null;
  }

}
