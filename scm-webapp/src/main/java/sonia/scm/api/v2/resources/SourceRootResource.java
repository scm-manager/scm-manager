package sonia.scm.api.v2.resources;

import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class SourceRootResource {

  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public SourceRootResource(RepositoryServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  @GET
  @Path("")
  public Response getAll(@PathParam("namespace") String namespace, @PathParam("name") String name) {

    BrowserResult browserResult = null;
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      BrowseCommandBuilder browseCommand = repositoryService.getBrowseCommand();
      browseCommand.setPath("/");
      browserResult = browseCommand.getBrowserResult();
    } catch (RepositoryNotFoundException e) {
      e.printStackTrace();
    } catch (RepositoryException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return Response.ok(browserResult.toString()).build();
  }

  @GET
  @Path("{revision}")
  public Response get() {
    throw new UnsupportedOperationException();
  }
}
