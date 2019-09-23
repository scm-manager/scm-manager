package sonia.scm.api.v2.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;

@Path("v2/plugins")
public class PluginRootResource {

  private Provider<InstalledPluginResource> installedPluginResourceProvider;
  private Provider<AvailablePluginResource> availablePluginResourceProvider;
  private Provider<PendingPluginResource> pendingPluginResourceProvider;

  @Inject
  public PluginRootResource(Provider<InstalledPluginResource> installedPluginResourceProvider, Provider<AvailablePluginResource> availablePluginResourceProvider, Provider<PendingPluginResource> pendingPluginResourceProvider) {
    this.installedPluginResourceProvider = installedPluginResourceProvider;
    this.availablePluginResourceProvider = availablePluginResourceProvider;
    this.pendingPluginResourceProvider = pendingPluginResourceProvider;
  }

  @Path("/installed")
  public InstalledPluginResource installedPlugins() {
    return installedPluginResourceProvider.get();
  }

  @Path("/available")
  public AvailablePluginResource availablePlugins() { return availablePluginResourceProvider.get(); }

  @Path("/pending")
  public PendingPluginResource pendingPlugins() { return pendingPluginResourceProvider.get(); }
}
