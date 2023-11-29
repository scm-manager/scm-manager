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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.apache.shiro.SecurityUtils;
import sonia.scm.NotFoundException;
import sonia.scm.metrics.MonitoringSystem;
import sonia.scm.metrics.ScrapeTarget;
import sonia.scm.web.VndMediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

/**
 * Endpoint for pull based monitoring systems, which scrape for metrics.
 * @since 2.15.0
 */
@OpenAPIDefinition(tags = {
  @Tag(name = "Metrics", description = "Scrape targets for monitoring systems")
})
@Path("v2/metrics")
public class MetricsResource {

  private final Map<String, MonitoringSystem> providers = new HashMap<>();

  @Inject
  public MetricsResource(Set<MonitoringSystem> monitoringSystems) {
    for (MonitoringSystem system : monitoringSystems) {
      providers.put(system.getName(), system);
    }
  }

  @GET
  @Path("{name}")
  @Operation(summary = "Scrape target", description = "Scrape target for a monitoring system", tags = "Metrics")
  @ApiResponse(
    responseCode = "200",
    description = "success"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "404", description = "unknown monitoring system or system without scrape target")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response metrics(@PathParam("name") String name) {
    SecurityUtils.getSubject().checkPermission("metrics:read");

    MonitoringSystem system = providers.get(name);
    if (system == null) {
      throw new NotFoundException(MonitoringSystem.class, name);
    }

    ScrapeTarget scrapeTarget = system.getScrapeTarget()
      .orElseThrow(() -> NotFoundException.notFound(entity(ScrapeTarget.class, name).in(MonitoringSystem.class, name)));

    StreamingOutput output = scrapeTarget::write;
    return Response.ok(output, scrapeTarget.getContentType()).build();
  }

}
