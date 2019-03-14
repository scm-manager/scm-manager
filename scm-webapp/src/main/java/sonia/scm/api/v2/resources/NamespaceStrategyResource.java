package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import sonia.scm.repository.NamespaceStrategy;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RESTFul WebService Endpoint for namespace strategies.
 */
@Path(NamespaceStrategyResource.PATH)
public class NamespaceStrategyResource {

  static final String PATH = "v2/namespaceStrategies";

  private Set<NamespaceStrategy> namespaceStrategies;
  private Provider<NamespaceStrategy> namespaceStrategyProvider;

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
   * @return available and current namespace strategies
   */
  @GET
  @Path("")
  @Produces(VndMediaType.NAMESPACE_STRATEGIES)
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
