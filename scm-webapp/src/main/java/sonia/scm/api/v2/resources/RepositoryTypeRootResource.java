package sonia.scm.api.v2.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;

/**
 * RESTful Web Service Resource to get available repository types.
 */
@Path(RepositoryTypeRootResource.PATH)
public class RepositoryTypeRootResource {

  static final String PATH = "v2/repository-types/";

  private Provider<RepositoryTypeCollectionResource> collectionResourceProvider;
  private Provider<RepositoryTypeResource> resourceProvider;

  @Inject
  public RepositoryTypeRootResource(Provider<RepositoryTypeCollectionResource> collectionResourceProvider, Provider<RepositoryTypeResource> resourceProvider) {
    this.collectionResourceProvider = collectionResourceProvider;
    this.resourceProvider = resourceProvider;
  }

  @Path("")
  public RepositoryTypeCollectionResource getRepositoryTypeCollectionResource() {
    return collectionResourceProvider.get();
  }

  @Path("{name}")
  public RepositoryTypeResource getRepositoryTypeResource() {
    return resourceProvider.get();
  }


}
