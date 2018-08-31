package sonia.scm.api.v2.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;

@Path("v2/ui")
public class UIRootResource {

  private Provider<UIPluginResource> uiPluginResourceProvider;

  @Inject
  public UIRootResource(Provider<UIPluginResource> uiPluginResourceProvider) {
    this.uiPluginResourceProvider = uiPluginResourceProvider;
  }

  @Path("plugins")
  public UIPluginResource plugins() {
    return uiPluginResourceProvider.get();
  }

}
