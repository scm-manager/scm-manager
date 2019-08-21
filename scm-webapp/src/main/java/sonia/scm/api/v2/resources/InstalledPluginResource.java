package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.InstalledPluginDescriptor;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginPermissions;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class InstalledPluginResource {

  private final PluginDtoCollectionMapper collectionMapper;
  private final PluginDtoMapper mapper;
  private final PluginManager pluginManager;

  @Inject
  public InstalledPluginResource(PluginManager pluginManager, PluginDtoCollectionMapper collectionMapper, PluginDtoMapper mapper) {
    this.pluginManager = pluginManager;
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
    PluginPermissions.read().check();
    List<InstalledPlugin> plugins = pluginManager.getInstalled();
    return Response.ok(collectionMapper.mapInstalled(plugins)).build();
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
    Optional<InstalledPlugin> pluginDto = pluginManager.getInstalled(name);
    if (pluginDto.isPresent()) {
      return Response.ok(mapper.mapInstalled(pluginDto.get())).build();
    } else {
      throw notFound(entity(InstalledPluginDescriptor.class, name));
    }
  }
}
