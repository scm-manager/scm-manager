package sonia.scm.api.v2.resources;

import io.swagger.v3.oas.annotations.Operation;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.NamespaceAndName;
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
import java.net.URLDecoder;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class SourceRootResource {

  private final RepositoryServiceFactory serviceFactory;
  private final BrowserResultToFileObjectDtoMapper browserResultToFileObjectDtoMapper;


  @Inject
  public SourceRootResource(RepositoryServiceFactory serviceFactory, BrowserResultToFileObjectDtoMapper browserResultToFileObjectDtoMapper) {
    this.serviceFactory = serviceFactory;
    this.browserResultToFileObjectDtoMapper = browserResultToFileObjectDtoMapper;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.SOURCE)
  @Operation(summary = "List of sources", description = "Returns all sources for repository head.", tags = "Repository")
  public Response getAllWithoutRevision(@PathParam("namespace") String namespace, @PathParam("name") String name) throws IOException {
    return getSource(namespace, name, "/", null);
  }

  @GET
  @Path("{revision}")
  @Produces(VndMediaType.SOURCE)
  @Operation(summary = "List of sources by revision", description = "Returns all sources for the given revision.", tags = "Repository")
  public Response getAll(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision) throws IOException {
    return getSource(namespace, name, "/", revision);
  }

  @GET
  @Path("{revision}/{path: .*}")
  @Produces(VndMediaType.SOURCE)
  @Operation(summary = "List of sources by revision in path", description = "Returns all sources for the given revision in a specific path.", tags = "Repository")
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision, @PathParam("path") String path) throws IOException {
    return getSource(namespace, name, path, revision);
  }

  private Response getSource(String namespace, String repoName, String path, String revision) throws IOException {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, repoName);
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      BrowseCommandBuilder browseCommand = repositoryService.getBrowseCommand();
      browseCommand.setPath(path);
      if (revision != null && !revision.isEmpty()) {
        browseCommand.setRevision(URLDecoder.decode(revision, "UTF-8"));
      }
      BrowserResult browserResult = browseCommand.getBrowserResult();

      if (browserResult != null) {
        return Response.ok(browserResultToFileObjectDtoMapper.map(browserResult, namespaceAndName)).build();
      } else {
        throw notFound(entity("Source", path).in("Revision", revision).in(namespaceAndName));
      }
    }
  }
}
