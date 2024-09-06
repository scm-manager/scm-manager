/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
