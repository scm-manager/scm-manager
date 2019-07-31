package sonia.scm.api.v2.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;

@Path("v2/plugins")
public class PluginRootResource {

  private Provider<InstalledPluginResource> installedPluginResourceProvider;
  private Provider<AvailablePluginResource> availablePluginResourceProvider;

  @Inject
  public PluginRootResource(Provider<InstalledPluginResource> installedPluginResourceProvider, Provider<AvailablePluginResource> availablePluginResourceProvider) {
    this.installedPluginResourceProvider = installedPluginResourceProvider;
    this.availablePluginResourceProvider = availablePluginResourceProvider;
  }

  @Path("/installed")
  public InstalledPluginResource installedPlugins() {
    return installedPluginResourceProvider.get();
  }

  @Path("/available")
  public AvailablePluginResource availablePlugins() { return availablePluginResourceProvider.get(); }
}
