package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.PageResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
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
import javax.ws.rs.core.Response;
import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

@Slf4j
public class FileHistoryRootResource {

  private final RepositoryServiceFactory serviceFactory;

  private final FileHistoryCollectionToDtoMapper fileHistoryCollectionToDtoMapper;


  @Inject
  public FileHistoryRootResource(RepositoryServiceFactory serviceFactory, FileHistoryCollectionToDtoMapper fileHistoryCollectionToDtoMapper) {
    this.serviceFactory = serviceFactory;
    this.fileHistoryCollectionToDtoMapper = fileHistoryCollectionToDtoMapper;
  }

  /**
   * Get all changesets related to the given file starting with the given revision
   *
   * @param namespace the repository namespace
   * @param name      the repository name
   * @param revision  the revision
   * @param path      the path of the file
   * @param page      pagination
   * @param pageSize  pagination
   * @return all changesets related to the given file starting with the given revision
   * @throws IOException                 on io error
   */
  @GET
  @Path("{revision}/{path: .*}")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the changeset"),
    @ResponseCode(code = 404, condition = "not found, no changesets available in the repository"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces(VndMediaType.CHANGESET_COLLECTION)
  @TypeHint(CollectionDto.class)
  public Response getAll(@PathParam("namespace") String namespace, @PathParam("name") String name,
                         @PathParam("revision") String revision,
                         @PathParam("path") String path,
                         @DefaultValue("0") @QueryParam("page") int page,
                         @DefaultValue("10") @QueryParam("pageSize") int pageSize) throws IOException {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      log.info("Get changesets of the file {} and revision {}", path, revision);
      Repository repository = repositoryService.getRepository();
      ChangesetPagingResult changesets = new PagedLogCommandBuilder(repositoryService)
        .page(page)
        .pageSize(pageSize)
        .create()
        .setPath(path)
        .setStartChangeset(revision)
        .getChangesets();
      if (changesets != null && changesets.getChangesets() != null) {
        PageResult<Changeset> pageResult = new PageResult<>(changesets.getChangesets(), changesets.getTotal());
        return Response.ok(fileHistoryCollectionToDtoMapper.map(page, pageSize, pageResult, repository, revision, path)).build();
      } else {
        String message = String.format("for the revision %s and the file %s there are no changesets", revision, path);
        log.error(message);
        throw notFound(entity("Path", path).in("revision", revision).in(namespaceAndName));
      }
    }
  }
}
