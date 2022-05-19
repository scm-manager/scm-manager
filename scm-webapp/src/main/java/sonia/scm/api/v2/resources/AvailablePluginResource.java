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
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginPermissions;
import sonia.scm.plugin.PluginSet;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
   *
   * @return collection of available plugins.
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
    List<InstalledPlugin> installed = pluginManager.getInstalled();
    List<AvailablePlugin> available = pluginManager.getAvailable().stream().filter(a -> notInstalled(a, installed)).collect(Collectors.toList());
    Set<PluginSet> pluginSets = pluginManager.getPluginSets();

    return Response.ok(collectionMapper.mapAvailable(available, pluginSets)).build();
  }

  private boolean notInstalled(AvailablePlugin a, List<InstalledPlugin> installed) {
    return installed.stream().noneMatch(installedPlugin -> installedPlugin.getDescriptor().getInformation().getName().equals(a.getDescriptor().getInformation().getName()));
  }

  /**
   * Returns available plugin.
   *
   * @return available plugin.
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
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"plugin:manage\" privilege")
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
