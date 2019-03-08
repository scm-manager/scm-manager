package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.MergeCommandBuilder;
import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.api.MergeDryRunCommandResult;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Slf4j
public class MergeResource {

  private final RepositoryServiceFactory serviceFactory;
  private final MergeResultToDtoMapper mapper;

  @Inject
  public MergeResource(RepositoryServiceFactory serviceFactory, MergeResultToDtoMapper mapper) {
    this.serviceFactory = serviceFactory;
    this.mapper = mapper;
  }

  @POST
  @Path("")
  @Produces(VndMediaType.MERGE_RESULT)
  @Consumes(VndMediaType.MERGE_COMMAND)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "merge has been executed successfully"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the privilege to write the repository"),
    @ResponseCode(code = 409, condition = "The branches could not be merged automatically due to conflicts (conflicting files will be returned)"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response merge(@PathParam("namespace") String namespace, @PathParam("name") String name, @Valid MergeCommandDto mergeCommand) {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    log.info("Merge in Repository {}/{} from {} to {}", namespace, name, mergeCommand.getSourceRevision(), mergeCommand.getTargetRevision());
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      MergeCommandResult mergeCommandResult = createMergeCommand(mergeCommand, repositoryService).executeMerge();
      if (mergeCommandResult.isSuccess()) {
        return Response.noContent().build();
      } else {
        return Response.status(HttpStatus.SC_CONFLICT).entity(mapper.map(mergeCommandResult)).build();
      }
    }
  }

  @POST
  @Path("dry-run/")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "merge can be done automatically"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 409, condition = "The branches can not be merged automatically due to conflicts"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response dryRun(@PathParam("namespace") String namespace, @PathParam("name") String name, @Valid MergeCommandDto mergeCommand) {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    log.info("Merge in Repository {}/{} from {} to {}", namespace, name, mergeCommand.getSourceRevision(), mergeCommand.getTargetRevision());
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      MergeDryRunCommandResult mergeCommandResult = createMergeCommand(mergeCommand, repositoryService).dryRun();
      if (mergeCommandResult.isMergeable()) {
        return Response.noContent().build();
      } else {
        throw new ConcurrentModificationException("revision", mergeCommand.getTargetRevision());
      }
    }
  }

  private MergeCommandBuilder createMergeCommand(MergeCommandDto mergeCommand, RepositoryService repositoryService) {
    return repositoryService
      .getMergeCommand()
      .setBranchToMerge(mergeCommand.getSourceRevision())
      .setTargetBranch(mergeCommand.getTargetRevision());
  }
}
