package sonia.scm.api.v2.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;

@Path("v2/")
public class PluginRootResource {

  private Provider<PluginResource> pluginResourceProvider;

  @Inject
  public PluginRootResource(Provider<PluginResource> pluginResourceProvider) {
    this.pluginResourceProvider = pluginResourceProvider;
  }

  @Path("plugins")
  public PluginResource plugins() {
    return pluginResourceProvider.get();
  }
}
