package sonia.scm.api.rest;

import sonia.scm.api.v2.resources.UriInfoStore;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class UriInfoFilter implements ContainerRequestFilter {

  private final javax.inject.Provider<UriInfoStore> storeProvider;

  @Inject
  public UriInfoFilter(javax.inject.Provider<UriInfoStore> storeProvider) {
    this.storeProvider = storeProvider;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    storeProvider.get().set(requestContext.getUriInfo());
  }
}
