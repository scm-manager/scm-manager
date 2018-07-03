package sonia.scm.api.v2.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;

/**
 *  RESTful Web Service Resource to manage repositories.
 */
@Path(RepositoryRootResource.REPOSITORIES_PATH_V2)
public class RepositoryRootResource {
  static final String REPOSITORIES_PATH_V2 = "v2/repositories/";

  private final Provider<RepositoryResource> repositoryResource;

  @Inject
  public RepositoryRootResource(Provider<RepositoryResource> repositoryResource) {
    this.repositoryResource = repositoryResource;
  }

  @Path("{namespace}/{name}")
  public RepositoryResource getRepositoryResource() {
    return repositoryResource.get();
  }
}
