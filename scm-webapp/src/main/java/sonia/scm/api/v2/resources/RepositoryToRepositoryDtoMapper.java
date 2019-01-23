package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.Feature;
import sonia.scm.repository.HealthCheckFailure;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;

import java.util.List;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.stream.Collectors.toList;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class RepositoryToRepositoryDtoMapper extends BaseMapper<Repository, RepositoryDto> {

  @Inject
  private ResourceLinks resourceLinks;
  @Inject
  private RepositoryServiceFactory serviceFactory;

  abstract HealthCheckFailureDto toDto(HealthCheckFailure failure);

  @AfterMapping
  void appendLinks(Repository repository, @MappingTarget RepositoryDto target) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.repository().self(target.getNamespace(), target.getName()));
    if (RepositoryPermissions.delete(repository).isPermitted()) {
      linksBuilder.single(link("delete", resourceLinks.repository().delete(target.getNamespace(), target.getName())));
    }
    if (RepositoryPermissions.modify(repository).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.repository().update(target.getNamespace(), target.getName())));
      linksBuilder.single(link("permissions", resourceLinks.repositoryPermission().all(target.getNamespace(), target.getName())));
    }
    try (RepositoryService repositoryService = serviceFactory.create(repository)) {
      if (RepositoryPermissions.pull(repository).isPermitted()) {
        List<Link> protocolLinks = repositoryService.getSupportedProtocols()
          .map(this::createProtocolLink)
          .collect(toList());
        linksBuilder.array(protocolLinks);
      }
      if (repositoryService.isSupported(Command.TAGS)) {
        linksBuilder.single(link("tags", resourceLinks.tag().all(target.getNamespace(), target.getName())));
      }
      if (repositoryService.isSupported(Command.BRANCHES)) {
        linksBuilder.single(link("branches", resourceLinks.branchCollection().self(target.getNamespace(), target.getName())));
      }
      if (repositoryService.isSupported(Feature.INCOMING_REVISION)) {
        linksBuilder.single(link("incomingChangesets", resourceLinks.incoming().changesets(target.getNamespace(), target.getName())));
        linksBuilder.single(link("incomingDiff", resourceLinks.incoming().diff(target.getNamespace(), target.getName())));
      }
      if (repositoryService.isSupported(Command.MERGE)) {
        linksBuilder.single(link("merge", resourceLinks.merge().merge(target.getNamespace(), target.getName())));
        linksBuilder.single(link("mergeDryRun", resourceLinks.merge().dryRun(target.getNamespace(), target.getName())));
      }
    }
    linksBuilder.single(link("changesets", resourceLinks.changeset().all(target.getNamespace(), target.getName())));
    linksBuilder.single(link("sources", resourceLinks.source().selfWithoutRevision(target.getNamespace(), target.getName())));

    appendLinks(new EdisonLinkAppender(linksBuilder), repository);

    target.add(linksBuilder.build());
  }

  private Link createProtocolLink(ScmProtocol protocol) {
    return Link.linkBuilder("protocol", protocol.getUrl()).withName(protocol.getType()).build();
  }
}
