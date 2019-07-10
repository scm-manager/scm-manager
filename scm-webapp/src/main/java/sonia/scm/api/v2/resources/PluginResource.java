package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.PluginPermissions;
import sonia.scm.plugin.PluginWrapper;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PluginResource {

  private final PluginLoader pluginLoader;
  private final PluginDtoCollectionMapper collectionMapper;
  private final PluginDtoMapper mapper;

  @Inject
  public PluginResource(PluginLoader pluginLoader, PluginDtoCollectionMapper collectionMapper, PluginDtoMapper mapper) {
    this.pluginLoader = pluginLoader;
    this.collectionMapper = collectionMapper;
    this.mapper = mapper;
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
    List<PluginWrapper> plugins = pluginLoader.getInstalledPlugins()
      .stream()
      .collect(Collectors.toList());
    PluginPermissions.read().check();
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
    Optional<PluginDto> pluginDto = pluginLoader.getInstalledPlugins()
      .stream()
      .filter(plugin -> id.equals(plugin.getId()))
      .map(mapper::map)
      .findFirst();
    PluginPermissions.read().check();
    if (pluginDto.isPresent()) {
      return Response.ok(pluginDto.get()).build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

}
