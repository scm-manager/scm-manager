package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import sonia.scm.NotFoundException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
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

public class DiffRootResource {

  public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

  static final String DIFF_FORMAT_VALUES_REGEX = "NATIVE|GIT|UNIFIED";

  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public DiffRootResource(RepositoryServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }


  /**
   * Get the repository diff of a revision
   *
   * @param namespace repository namespace
   * @param name repository name
   * @param revision the revision
   * @return the dif of the revision
   * @throws NotFoundException if the repository is not found
   */
  @GET
  @Path("{revision}")
  @Produces(VndMediaType.DIFF)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "Bad Request"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the diff"),
    @ResponseCode(code = 404, condition = "not found, no revision with the specified param for the repository available or repository not found"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("revision") String revision , @Pattern(regexp = DIFF_FORMAT_VALUES_REGEX) @DefaultValue("NATIVE") @QueryParam("format") String format ) throws IOException {
    HttpUtil.checkForCRLFInjection(revision);
    DiffFormat diffFormat = DiffFormat.valueOf(format);
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      DiffCommandBuilder.OutputStreamConsumer outputStreamConsumer = repositoryService.getDiffCommand()
        .setRevision(revision)
        .setFormat(diffFormat)
        .retrieveContent();
      return Response.ok((StreamingOutput) outputStreamConsumer::accept)
        .header(HEADER_CONTENT_DISPOSITION, HttpUtil.createContentDispositionAttachmentHeader(String.format("%s-%s.diff", name, revision)))
        .build();
    }
  }
}
