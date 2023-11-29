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
   *
   * @return collection of installed plugins.
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
   * @return installed plugin with specified id
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
