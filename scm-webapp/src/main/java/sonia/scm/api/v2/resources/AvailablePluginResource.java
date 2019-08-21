package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.InstalledPluginDescriptor;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginPermissions;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

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
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(CollectionDto.class)
  @Produces(VndMediaType.PLUGIN_COLLECTION)
  public Response getAvailablePlugins() {
    PluginPermissions.read().check();
    List<AvailablePlugin> available = pluginManager.getAvailable();
    return Response.ok(collectionMapper.mapAvailable(available)).build();
  }

  /**
   * Returns available plugin.
   *
   * @return available plugin.
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
  public Response getAvailablePlugin(@PathParam("name") String name) {
    PluginPermissions.read().check();
    Optional<AvailablePlugin> plugin = pluginManager.getAvailable(name);
    if (plugin.isPresent()) {
      return Response.ok(mapper.map(plugin.get())).build();
    } else {
      throw notFound(entity(InstalledPluginDescriptor.class, name));
    }
  }

  /**
   * Triggers plugin installation.
   * @param name plugin name
   * @return HTTP Status.
   */
  @POST
  @Path("/{name}/install")
  @Consumes(VndMediaType.PLUGIN)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response installPlugin(@PathParam("name") String name) {
    PluginPermissions.manage().check();
    pluginManager.install(name);
    return Response.ok().build();
  }
}
