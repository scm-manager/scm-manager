/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.api.v2.resources;

import com.cronutils.utils.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;
import sonia.scm.SCMContextProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.util.SystemUtil;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
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
@OpenAPIDefinition(tags = {
  @Tag(name = "Alerts", description = "Alert related endpoints")
})
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

  @GET
  @Path("")
  @Produces(VndMediaType.ALERTS_REQUEST)
  @Operation(
    summary = "Alerts",
    description = "Returns url and body prepared for the alert service",
    tags = "Alerts",
    operationId = "alerts_get_request"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.ALERTS_REQUEST,
      schema = @Schema(implementation = HalRepresentation.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public AlertsRequest getAlertsRequest(@Context UriInfo uriInfo) throws IOException {
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

    Links links = createLinks(uriInfo, url);
    return new AlertsRequest(links, checksum, body);
  }

  private Links createLinks(UriInfo uriInfo, String alertsUrl) {
    return Links.linkingTo()
      .self(uriInfo.getAbsolutePath().toASCIIString())
      .single(Link.link("alerts", alertsUrl))
      .build();
  }

  @SuppressWarnings("UnstableApiUsage")
  private String createChecksum(String url, AlertsRequestBody body) throws IOException {
    Hasher hasher = Hashing.sha256().newHasher();
    hasher.putString(url, StandardCharsets.UTF_8);
    hasher.putString(dateSupplier.get(), StandardCharsets.UTF_8);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(baos)) {
      out.writeObject(body);
    }

    hasher.putBytes(baos.toByteArray());
    return hasher.hash().toString();
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @SuppressWarnings("java:S2160") // we need no equals here
  public static class AlertsRequest extends HalRepresentation {

    private String checksum;
    private AlertsRequestBody body;

    public AlertsRequest(Links links, String checksum, AlertsRequestBody body) {
      super(links);
      this.checksum = checksum;
      this.body = body;
    }
  }

  @Value
  public static class AlertsRequestBody implements Serializable {

    String instanceId;
    String version;
    String os;
    String arch;
    String jre;
    @SuppressWarnings("java:S1948") // the field is serializable, but sonar does not get it
    List<Plugin> plugins;

  }

  @Value
  public static class Plugin implements Serializable {

    String name;
    String version;

  }

}
