package sonia.scm.api.v2.resources;

import com.cronutils.utils.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import lombok.Value;
import sonia.scm.SCMContextProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.util.SystemUtil;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Path("v2/alerts")
public class AlertsResource {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final SCMContextProvider scmContextProvider;
  private final ScmConfiguration scmConfiguration;
  private final PluginLoader pluginLoader;
  private final Supplier<String> dateSupplier;

  @Inject
  public AlertsResource(SCMContextProvider scmContextProvider, ScmConfiguration scmConfiguration, PluginLoader pluginLoader) {
    this(scmContextProvider, scmConfiguration, pluginLoader, () -> LocalDateTime.now().format(FORMATTER));
  }

  @VisibleForTesting
  AlertsResource(SCMContextProvider scmContextProvider, ScmConfiguration scmConfiguration, PluginLoader pluginLoader, Supplier<String> dateSupplier) {
    this.scmContextProvider = scmContextProvider;
    this.scmConfiguration = scmConfiguration;
    this.pluginLoader = pluginLoader;
    this.dateSupplier = dateSupplier;
  }

  @Path("")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public AlertsRequest getAlertsRequest() {
    if (Strings.isNullOrEmpty(scmConfiguration.getAlertsUrl())) {
      throw new WebApplicationException("Alerts disabled", Response.Status.CONFLICT);
    }

    String instanceId = scmContextProvider.getInstanceId();
    String version = scmContextProvider.getVersion();
    String os = SystemUtil.getOS();
    String arch = SystemUtil.getArch();
    String jre = SystemUtil.getJre();

    List<Plugin> plugins = pluginLoader.getInstalledPlugins().stream()
      .map(p -> p.getDescriptor().getInformation())
      .map(i -> new Plugin(i.getName(), i.getVersion()))
      .collect(Collectors.toList());

    String url = scmConfiguration.getAlertsUrl();
    AlertsRequestBody body = new AlertsRequestBody(instanceId, version, os, arch, jre, plugins);
    String checksum = createChecksum(url, body);
    return new AlertsRequest(url, checksum, body);
  }

  @SuppressWarnings("UnstableApiUsage")
  private String createChecksum(String url, AlertsRequestBody body) {
    Hasher hasher = Hashing.sha256().newHasher();
    hasher.putString(url, StandardCharsets.UTF_8);
    hasher.putString(dateSupplier.get(), StandardCharsets.UTF_8);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(baos)) {
      out.writeObject(body);
    } catch (IOException e) {
      e.printStackTrace();
    }

    hasher.putBytes(baos.toByteArray());
    return hasher.hash().toString();
  }

  @Value
  public static class AlertsRequest {

    String url;
    String checksum;
    AlertsRequestBody body;

  }

  @Value
  public static class AlertsRequestBody implements Serializable {

    String instanceId;
    String version;
    String os;
    String arch;
    String jre;
    List<Plugin> plugins;

  }

  @Value
  public static class Plugin implements Serializable {

    String name;
    String version;

  }

}
