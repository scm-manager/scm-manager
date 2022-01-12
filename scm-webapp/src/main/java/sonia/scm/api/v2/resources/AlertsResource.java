package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.util.SystemUtil;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("v2/alerts")
public class AlertsResource {

  private final SCMContextProvider scmContextProvider;
  private final PluginLoader pluginLoader;

  @Inject
  public AlertsResource(SCMContextProvider scmContextProvider, PluginLoader pluginLoader) {
    this.scmContextProvider = scmContextProvider;
    this.pluginLoader = pluginLoader;
  }

  @Path("")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public AlertsRequest getAlertsRequest() {
    String instanceId = scmContextProvider.getInstanceId();
    String version = scmContextProvider.getVersion();
    String os = SystemUtil.getOS();
    String arch = SystemUtil.getArch();
    String jre = SystemUtil.getJre();

    List<Plugin> plugins = pluginLoader.getInstalledPlugins().stream()
      .map(p -> p.getDescriptor().getInformation())
      .map(i -> new Plugin(i.getName(), i.getVersion()))
      .collect(Collectors.toList());

    AlertsRequestBody body = new AlertsRequestBody(instanceId, version, os, arch, jre, plugins);
    return new AlertsRequest("https://alerts.scm-manager.org/api/v1/alerts", body);
  }

  @Value
  public static class AlertsRequest {

    String url;
    AlertsRequestBody body;

  }

  @Value
  public static class AlertsRequestBody {

    String instanceId;
    String version;
    String os;
    String arch;
    String jre;
    List<Plugin> plugins;

  }

  @Value
  public static class Plugin {

    String name;
    String version;

  }

}
