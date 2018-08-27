package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import de.otto.edison.hal.HalRepresentation;
import sonia.scm.plugin.PluginLoader;
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
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(CollectionDto.class)
  @Produces(VndMediaType.UI_PLUGIN_COLLECTION)
  public Response getInstalledPlugins() {
    List<PluginWrapper> plugins = pluginLoader.getInstalledPlugins()
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
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 404, condition = "not found"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(UIPluginDto.class)
  @Produces(VndMediaType.UI_PLUGIN)
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

  private boolean filter(PluginWrapper plugin) {
    return plugin.getPlugin().getResources() != null;
  }

}
