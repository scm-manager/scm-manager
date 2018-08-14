package sonia.scm.api.v2.resources;

import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class SourceRootResource {

  private final RepositoryServiceFactory serviceFactory;
  private final BrowserResultMapper browserResultMapper;


  @Inject
  public SourceRootResource(RepositoryServiceFactory serviceFactory, BrowserResultMapper browserResultMapper) {
    this.serviceFactory = serviceFactory;
    this.browserResultMapper = browserResultMapper;
  }

  @GET
  @Produces(VndMediaType.SOURCE)
  @Path("")
  public Response getAll(@PathParam("namespace") String namespace, @PathParam("name") String name) {

    BrowserResult browserResult;
    Response response;
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      BrowseCommandBuilder browseCommand = repositoryService.getBrowseCommand();
      browseCommand.setPath("/");
      browserResult = browseCommand.getBrowserResult();

      if (browserResult != null) {
        response = Response.ok(browserResultMapper.map(browserResult, namespaceAndName)).build();
      } else {
        response = Response.status(Response.Status.NOT_FOUND).build();
      }

    } catch (RepositoryNotFoundException e) {
      response = Response.status(Response.Status.NOT_FOUND).build();
    } catch (RepositoryException | IOException e) {
      response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    return response;
  }

  @GET
  @Path("{revision}/{path: .*}")
  public Response get() {
    throw new UnsupportedOperationException();
  }
}
