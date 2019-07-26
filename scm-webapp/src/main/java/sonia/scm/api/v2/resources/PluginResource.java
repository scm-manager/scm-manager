package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.plugin.Plugin;
import sonia.scm.plugin.PluginCenter;
import sonia.scm.plugin.PluginInformation;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class PluginResource {

  private final PluginLoader pluginLoader;
  private final PluginDtoCollectionMapper collectionMapper;
  private final PluginDtoMapper mapper;
  private final PluginManager pluginManager;

  @Inject
  public PluginResource(PluginLoader pluginLoader, PluginDtoCollectionMapper collectionMapper, PluginDtoMapper mapper, PluginCenter pluginCenter1, PluginManager pluginManager) {
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
   * @param id id of plugin
   *
   * @return installed plugin with specified id
   */
  @GET
  @Path("{id}")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 404, condition = "not found"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(PluginDto.class)
  @Produces(VndMediaType.PLUGIN)
  public Response getInstalledPlugin(@PathParam("id") String id) {
    PluginPermissions.read().check();
    Optional<PluginDto> pluginDto = pluginLoader.getInstalledPlugins()
      .stream()
      .filter(plugin -> id.equals(plugin.getPlugin().getInformation().getName(false)))
      .map(mapper::map)
      .findFirst();
    if (pluginDto.isPresent()) {
      return Response.ok(pluginDto.get()).build();
    } else {
      throw notFound(entity(Plugin.class, id));
    }
  }

  /**
   * Returns a collection of available plugins.
   *
   * @return collection of available plugins.
   */
  @GET
  @Path("/available")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(CollectionDto.class)
  @Produces(VndMediaType.PLUGIN_COLLECTION)
  public Response getAvailablePlugins() {
    PluginPermissions.read().check();
    Collection<PluginInformation> plugins = pluginManager.getAvailable();
    return Response.ok(collectionMapper.map(plugins)).build();
  }

}
