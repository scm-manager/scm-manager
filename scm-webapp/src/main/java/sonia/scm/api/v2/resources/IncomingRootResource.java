package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.PageResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.VndMediaType;

import javax.validation.constraints.Pattern;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;

import static sonia.scm.api.v2.resources.DiffRootResource.DIFF_FORMAT_VALUES_REGEX;
import static sonia.scm.api.v2.resources.DiffRootResource.HEADER_CONTENT_DISPOSITION;

public class IncomingRootResource {


  private final RepositoryServiceFactory serviceFactory;

  private final IncomingChangesetCollectionToDtoMapper mapper;


  @Inject
  public IncomingRootResource(RepositoryServiceFactory serviceFactory, IncomingChangesetCollectionToDtoMapper incomingChangesetCollectionToDtoMapper) {
    this.serviceFactory = serviceFactory;
    this.mapper = incomingChangesetCollectionToDtoMapper;
  }

  /**
   * Get the incoming changesets from <code>source</code> to <code>target</code>
   * <p>
   * Example:
   * <p>
   * -                          master
   * -                            |
   * -            _______________ ° m1
   * -           e                |
   * -           |                ° m2
   * -           ° e1             |
   * -    ______|_______          |
   * -   |             |          b
   * -   f             a          |
   * -   |             |          ° b1
   * -   ° f1          ° a1       |
   * -                            ° b2
   * -
   * <p>
   * - /incoming/a/master/changesets  -> a1 , e1
   * - /incoming/b/master/changesets  -> b1 , b2
   * - /incoming/b/f/changesets       -> b1 , b2, m2
   * - /incoming/f/b/changesets       -> f1 , e1
   * - /incoming/a/b/changesets       -> a1 , e1
   * - /incoming/a/b/changesets       -> a1 , e1
   *
   * @param namespace
   * @param name
   * @param source    can be a changeset id or a branch name
   * @param target    can be a changeset id or a branch name
   * @param page
   * @param pageSize
   * @return
   * @throws Exception
   */
  @Path("{source}/{target}/changesets")
  @GET
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the changeset"),
    @ResponseCode(code = 404, condition = "not found, no changesets available in the repository"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces(VndMediaType.CHANGESET_COLLECTION)
  @TypeHint(CollectionDto.class)
  public Response incomingChangesets(@PathParam("namespace") String namespace,
                                     @PathParam("name") String name,
                                     @PathParam("source") String source,
                                     @PathParam("target") String target,
                                     @DefaultValue("0") @QueryParam("page") int page,
                                     @DefaultValue("10") @QueryParam("pageSize") int pageSize) throws IOException {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Repository repository = repositoryService.getRepository();
      RepositoryPermissions.read(repository).check();
      ChangesetPagingResult changesets = new PagedLogCommandBuilder(repositoryService)
        .page(page)
        .pageSize(pageSize)
        .create()
        .setStartChangeset(source)
        .setAncestorChangeset(target)
        .getChangesets();
      if (changesets != null && changesets.getChangesets() != null) {
        PageResult<Changeset> pageResult = new PageResult<>(changesets.getChangesets(), changesets.getTotal());
        return Response.ok(mapper.map(page, pageSize, pageResult, repository, source, target)).build();
      } else {
        return Response.ok().build();
      }
    }
  }


  @Path("{source}/{target}/diff")
  @GET
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the changeset"),
    @ResponseCode(code = 404, condition = "not found, no changesets available in the repository"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces(VndMediaType.DIFF)
  @TypeHint(CollectionDto.class)
  public Response incomingDiff(@PathParam("namespace") String namespace,
                               @PathParam("name") String name,
                               @PathParam("source") String source,
                               @PathParam("target") String target,
                               @Pattern(regexp = DIFF_FORMAT_VALUES_REGEX) @DefaultValue("NATIVE") @QueryParam("format") String format) throws IOException {


    HttpUtil.checkForCRLFInjection(source);
    HttpUtil.checkForCRLFInjection(target);
    DiffFormat diffFormat = DiffFormat.valueOf(format);
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      StreamingOutput responseEntry = output ->
        repositoryService.getDiffCommand()
          .setRevision(source)
          .setAncestorChangeset(target)
          .setFormat(diffFormat)
          .retrieveContent(output);

      return Response.ok(responseEntry)
        .header(HEADER_CONTENT_DISPOSITION, HttpUtil.createContentDispositionAttachmentHeader(String.format("%s-%s.diff", name, source)))
        .build();
    }
  }
}
