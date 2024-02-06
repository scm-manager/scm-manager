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

import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import sonia.scm.repository.NamespaceStrategy;
import sonia.scm.web.VndMediaType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RESTFul WebService Endpoint for namespace strategies.
 */
@Path(NamespaceStrategyResource.PATH)
public class NamespaceStrategyResource {

  static final String PATH = "v2/namespaceStrategies";

  private final Set<NamespaceStrategy> namespaceStrategies;
  private final Provider<NamespaceStrategy> namespaceStrategyProvider;

  @Inject
  public NamespaceStrategyResource(Set<NamespaceStrategy> namespaceStrategies, Provider<NamespaceStrategy> namespaceStrategyProvider) {
    this.namespaceStrategies = namespaceStrategies;
    this.namespaceStrategyProvider = namespaceStrategyProvider;
  }

  /**
   * Returns all available namespace strategies and the current selected.
   *
   * @param uriInfo uri info
   *
   */
  @GET
  @Path("")
  @Produces(VndMediaType.NAMESPACE_STRATEGIES)
  @Operation(summary = "List of namespace strategies", description = "Returns all available namespace strategies and the current selected.", tags = "Repository")
  public NamespaceStrategiesDto get(@Context UriInfo uriInfo) {
    String currentStrategy = strategyAsString(namespaceStrategyProvider.get());
    List<String> availableStrategies = collectStrategyNames();

    return new NamespaceStrategiesDto(currentStrategy, availableStrategies, createLinks(uriInfo));
  }

  private Links createLinks(@Context UriInfo uriInfo) {
    return Links.linkingTo().self(uriInfo.getAbsolutePath().toASCIIString()).build();
  }

  private String strategyAsString(NamespaceStrategy namespaceStrategy) {
    return namespaceStrategy.getClass().getSimpleName();
  }

  private List<String> collectStrategyNames() {
    return namespaceStrategies.stream().map(this::strategyAsString).collect(Collectors.toList());
  }
}
