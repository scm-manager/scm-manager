package sonia.scm.api.v2.resources;

import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
  @Produces(VndMediaType.SOURCE)
  @Path("")
  public FileObjectDto getAllWithoutRevision(@PathParam("namespace") String namespace, @PathParam("name") String name, @DefaultValue("0") @QueryParam("proceedFrom") int proceedFrom) throws IOException {
    return getSource(namespace, name, "/", null, proceedFrom);
  }

  @GET
  @Produces(VndMediaType.SOURCE)
  @Path("{revision}")
  public FileObjectDto getAll(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision, @DefaultValue("0") @QueryParam("proceedFrom") int proceedFrom) throws IOException {
    return getSource(namespace, name, "/", revision, proceedFrom);
  }

  @GET
  @Produces(VndMediaType.SOURCE)
  @Path("{revision}/{path: .*}")
  public FileObjectDto get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision, @PathParam("path") String path, @DefaultValue("0") @QueryParam("proceedFrom") int proceedFrom) throws IOException {
    return getSource(namespace, name, path, revision, proceedFrom);
  }

  private FileObjectDto getSource(String namespace, String repoName, String path, String revision, int proceedFrom) throws IOException {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, repoName);
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      BrowseCommandBuilder browseCommand = repositoryService.getBrowseCommand();
      browseCommand.setPath(path);
      browseCommand.setProceedFrom(proceedFrom);
      if (revision != null && !revision.isEmpty()) {
        browseCommand.setRevision(URLDecoder.decode(revision, "UTF-8"));
      }
      BrowserResult browserResult = browseCommand.getBrowserResult();

      if (browserResult != null) {
        return browserResultToFileObjectDtoMapper.map(browserResult, namespaceAndName, proceedFrom);
      } else {
        throw notFound(entity("Source", path).in("Revision", revision).in(namespaceAndName));
      }
    }
  }
}
