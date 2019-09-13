package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginPermissions;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.stream.Collectors.toList;

public class PendingPluginResource {

  private final PluginManager pluginManager;
  private final ResourceLinks resourceLinks;
  private final PluginDtoMapper mapper;

  @Inject
  public PendingPluginResource(PluginManager pluginManager, ResourceLinks resourceLinks, PluginDtoMapper mapper) {
    this.pluginManager = pluginManager;
    this.resourceLinks = resourceLinks;
    this.mapper = mapper;
  }

  @GET
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces(VndMediaType.PLUGIN_COLLECTION)
  public Response getPending() {
    PluginPermissions.manage().check();

    List<AvailablePlugin> pending = pluginManager
      .getAvailable()
      .stream()
      .filter(AvailablePlugin::isPending)
      .collect(toList());

    Links.Builder linksBuilder = linkingTo().self(resourceLinks.pendingPluginCollection().self());

    if (!pending.isEmpty()) {
      linksBuilder.single(link("install", resourceLinks.pendingPluginCollection().installPending()));
    }

    Embedded.Builder embedded = Embedded.embeddedBuilder();
    embedded.with("available", pending.stream().map(mapper::mapAvailable).collect(toList()));

    return Response.ok(new HalRepresentation(linksBuilder.build(), embedded.build())).build();
  }

  @POST
  @Path("/install")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response installPending() {
    PluginPermissions.manage().check();
    pluginManager.installPendingAndRestart();
    return Response.ok().build();
  }
}
