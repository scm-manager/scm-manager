package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.Feature;
import sonia.scm.repository.HealthCheckFailure;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;

import java.util.List;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
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

  @ObjectFactory
  RepositoryDto createDto(Repository repository) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.repository().self(repository.getNamespace(), repository.getName()));
    if (RepositoryPermissions.delete(repository).isPermitted()) {
      linksBuilder.single(link("delete", resourceLinks.repository().delete(repository.getNamespace(), repository.getName())));
    }
    if (RepositoryPermissions.modify(repository).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.repository().update(repository.getNamespace(), repository.getName())));
    }
    if (RepositoryPermissions.permissionRead(repository).isPermitted()) {
      linksBuilder.single(link("permissions", resourceLinks.repositoryPermission().all(repository.getNamespace(), repository.getName())));
    }
    try (RepositoryService repositoryService = serviceFactory.create(repository)) {
      if (RepositoryPermissions.pull(repository).isPermitted()) {
        List<Link> protocolLinks = repositoryService.getSupportedProtocols()
          .map(this::createProtocolLink)
          .collect(toList());
        linksBuilder.array(protocolLinks);
      }
      if (repositoryService.isSupported(Command.TAGS)) {
        linksBuilder.single(link("tags", resourceLinks.tag().all(repository.getNamespace(), repository.getName())));
      }
      if (repositoryService.isSupported(Command.BRANCHES)) {
        linksBuilder.single(link("branches", resourceLinks.branchCollection().self(repository.getNamespace(), repository.getName())));
      }
      if (repositoryService.isSupported(Feature.INCOMING_REVISION)) {
        linksBuilder.single(link("incomingChangesets", resourceLinks.incoming().changesets(repository.getNamespace(), repository.getName())));
        linksBuilder.single(link("incomingDiff", resourceLinks.incoming().diff(repository.getNamespace(), repository.getName())));
      }
      if (repositoryService.isSupported(Command.MERGE)) {
        if (RepositoryPermissions.push(repository).isPermitted()) {
          linksBuilder.single(link("merge", resourceLinks.merge().merge(repository.getNamespace(), repository.getName())));
        }
        linksBuilder.single(link("mergeDryRun", resourceLinks.merge().dryRun(repository.getNamespace(), repository.getName())));
      }
    }
    linksBuilder.single(link("changesets", resourceLinks.changeset().all(repository.getNamespace(), repository.getName())));
    linksBuilder.single(link("sources", resourceLinks.source().selfWithoutRevision(repository.getNamespace(), repository.getName())));

    Embedded.Builder embeddedBuilder = embeddedBuilder();
    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), repository);

    return new RepositoryDto(linksBuilder.build(), embeddedBuilder.build());
  }

  private Link createProtocolLink(ScmProtocol protocol) {
    return Link.linkBuilder("protocol", protocol.getUrl()).withName(protocol.getType()).build();
  }
}
