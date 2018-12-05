package sonia.scm.api.v2.resources;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
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
  @Produces(VndMediaType.MERGE_RESULT)
  @Consumes(VndMediaType.MERGE_COMMAND)
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
  public Response dryRun(@PathParam("namespace") String namespace, @PathParam("name") String name, @Valid MergeCommandDto mergeCommand) {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    log.info("Merge in Repository {}/{} from {} to {}", namespace, name, mergeCommand.getSourceRevision(), mergeCommand.getTargetRevision());
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      MergeDryRunCommandResult mergeCommandResult = createMergeCommand(mergeCommand, repositoryService).dryRun();
      if (mergeCommandResult.isMergeable()) {
        return Response.noContent().build();
      } else {
        return Response.status(HttpStatus.SC_CONFLICT).build();
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
