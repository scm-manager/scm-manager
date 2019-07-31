package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.plugin.Plugin;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginPermissions;
import sonia.scm.plugin.PluginWrapper;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class InstalledPluginResource {

  private final PluginLoader pluginLoader;
  private final PluginDtoCollectionMapper collectionMapper;
  private final PluginDtoMapper mapper;
  private final PluginManager pluginManager;

  @Inject
  public InstalledPluginResource(PluginLoader pluginLoader, PluginDtoCollectionMapper collectionMapper, PluginDtoMapper mapper, PluginManager pluginManager) {
    this.pluginLoader = pluginLoader;
    this.collectionMapper = collectionMapper;
    this.mapper = mapper;
    this.pluginManager = pluginManager;
  }

  /**
   * Returns a collection of installed plugins.
   *
   * @return collection of installed plugins.
   */
  @GET
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(CollectionDto.class)
  @Produces(VndMediaType.PLUGIN_COLLECTION)
  public Response getInstalledPlugins() {
    PluginPermissions.read().check();
    List<PluginWrapper> plugins = new ArrayList<>(pluginLoader.getInstalledPlugins());
    return Response.ok(collectionMapper.map(plugins)).build();
  }

  /**
   * Returns the installed plugin with the given id.
   *
   * @param name name of plugin
   *
   * @return installed plugin with specified id
   */
  @GET
  @Path("/{name}")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 404, condition = "not found"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(PluginDto.class)
  @Produces(VndMediaType.PLUGIN)
  public Response getInstalledPlugin(@PathParam("name") String name) {
    PluginPermissions.read().check();
    Optional<PluginDto> pluginDto = pluginLoader.getInstalledPlugins()
      .stream()
      .filter(plugin -> name.equals(plugin.getPlugin().getInformation().getName()))
      .map(mapper::map)
      .findFirst();
    if (pluginDto.isPresent()) {
      return Response.ok(pluginDto.get()).build();
    } else {
      throw notFound(entity(Plugin.class, name));
    }
  }
}
