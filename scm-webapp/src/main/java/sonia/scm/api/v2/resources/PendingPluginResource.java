package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginPermissions;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

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
    List<InstalledPlugin> installed = pluginManager.getInstalled();

    Stream<AvailablePlugin> newPlugins = pending
      .stream()
      .filter(a -> !contains(installed, a));
    Stream<InstalledPlugin> updatePlugins = installed
      .stream()
      .filter(i -> contains(pending, i));
    Stream<InstalledPlugin> uninstallPlugins = installed
      .stream()
      .filter(InstalledPlugin::isMarkedForUninstall);

    Links.Builder linksBuilder = linkingTo().self(resourceLinks.pendingPluginCollection().self());

    List<PluginDto> installDtos = newPlugins.map(mapper::mapAvailable).collect(toList());
    List<PluginDto> updateDtos = updatePlugins.map(i -> mapper.mapInstalled(i, pending)).collect(toList());
    List<PluginDto> uninstallDtos = uninstallPlugins.map(i -> mapper.mapInstalled(i, pending)).collect(toList());

    if (!installDtos.isEmpty() || !updateDtos.isEmpty() || !uninstallDtos.isEmpty()) {
      linksBuilder.single(link("execute", resourceLinks.pendingPluginCollection().executePending()));
    }

    Embedded.Builder embedded = Embedded.embeddedBuilder();
    embedded.with("new", installDtos);
    embedded.with("update", updateDtos);
    embedded.with("uninstall", uninstallDtos);

    return Response.ok(new HalRepresentation(linksBuilder.build(), embedded.build())).build();
  }

  private boolean contains(Collection<InstalledPlugin> installedPlugins, AvailablePlugin availablePlugin) {
    return installedPlugins
      .stream()
      .anyMatch(installedPlugin -> haveSameName(installedPlugin, availablePlugin));
  }

  private boolean contains(Collection<AvailablePlugin> availablePlugins, InstalledPlugin installedPlugin) {
    return availablePlugins
      .stream()
      .anyMatch(availablePlugin -> haveSameName(installedPlugin, availablePlugin));
  }

  private boolean haveSameName(InstalledPlugin installedPlugin, AvailablePlugin availablePlugin) {
    return installedPlugin.getDescriptor().getInformation().getName().equals(availablePlugin.getDescriptor().getInformation().getName());
  }

  @POST
  @Path("/execute")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response executePending() {
    PluginPermissions.manage().check();
    pluginManager.executePendingAndRestart();
    return Response.ok().build();
  }
}
