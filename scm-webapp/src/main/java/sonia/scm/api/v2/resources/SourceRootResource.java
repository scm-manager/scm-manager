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
  public Response getAllWithoutRevision(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    return getSource(namespace, name, "/", null);
  }

  @GET
  @Produces(VndMediaType.SOURCE)
  @Path("{revision}")
  public Response getAll(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision) {
    return getSource(namespace, name, "/", revision);
  }

  @GET
  @Produces(VndMediaType.SOURCE)
  @Path("{revision}/{path: .*}")
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision, @PathParam("path") String path) {
    return getSource(namespace, name, path, revision);
  }

  private Response getSource(String namespace, String repoName, String path, String revision) {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, repoName);
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      BrowseCommandBuilder browseCommand = repositoryService.getBrowseCommand();
      browseCommand.setPath(path);
      if (revision != null && !revision.isEmpty()) {
        browseCommand.setRevision(revision);
      }
      BrowserResult browserResult = browseCommand.getBrowserResult();

      if (browserResult != null) {
        return Response.ok(browserResultMapper.map(browserResult, namespaceAndName)).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

    } catch (RepositoryNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (RepositoryException | IOException e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
}
