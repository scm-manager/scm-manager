package sonia.scm.api.v2.resources;

import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.PluginWrapper;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("v2/ui")
public class UIRootResource {

  private final PluginLoader pluginLoader;

  @Inject
  public UIRootResource(PluginLoader pluginLoader) {
    this.pluginLoader = pluginLoader;
  }

  @GET
  @Path("plugins")
  @Produces(MediaType.APPLICATION_JSON)
  public List<UIPluginDto> getInstalledPlugins() {
    return pluginLoader.getInstalledPlugins()
      .stream()
      .filter(this::filter)
      .map(this::map)
      .collect(Collectors.toList());
  }

  private boolean filter(PluginWrapper plugin) {
    return plugin.getPlugin().getResources() != null;
  }

  private UIPluginDto map(PluginWrapper plugin) {
    return new UIPluginDto(
      plugin.getPlugin().getInformation().getName(),
      plugin.getPlugin().getResources().getScriptResources()
    );
  }

}
