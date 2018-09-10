package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.HealthCheckFailure;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;

import java.util.Collection;
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
  @Inject
  private UriInfoStore uriInfoStore;

  abstract HealthCheckFailureDto toDto(HealthCheckFailure failure);

  @AfterMapping
  void appendLinks(Repository repository, @MappingTarget RepositoryDto target) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.repository().self(target.getNamespace(), target.getName()));
    if (RepositoryPermissions.delete(repository).isPermitted()) {
      linksBuilder.single(link("delete", resourceLinks.repository().delete(target.getNamespace(), target.getName())));
    }
    if (RepositoryPermissions.modify(repository).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.repository().update(target.getNamespace(), target.getName())));
      linksBuilder.single(link("permissions", resourceLinks.permission().all(target.getNamespace(), target.getName())));
    }
    try (RepositoryService repositoryService = serviceFactory.create(repository)) {
      if (RepositoryPermissions.pull(repository).isPermitted()) {
        Collection<ScmProtocol> supportedProtocols = repositoryService.getSupportedProtocols();
        List<Link> protocolLinks = supportedProtocols
          .stream()
          .map(protocol -> createProtocolLink(protocol, repository))
          .collect(toList());
        linksBuilder.array(protocolLinks);
      }
      if (repositoryService.isSupported(Command.TAGS)) {
        linksBuilder.single(link("tags", resourceLinks.tag().all(target.getNamespace(), target.getName())));
      }
      if (repositoryService.isSupported(Command.BRANCHES)) {
        linksBuilder.single(link("branches", resourceLinks.branchCollection().self(target.getNamespace(), target.getName())));
      }
    }
    linksBuilder.single(link("changesets", resourceLinks.changeset().all(target.getNamespace(), target.getName())));
    linksBuilder.single(link("sources", resourceLinks.source().selfWithoutRevision(target.getNamespace(), target.getName())));
    target.add(linksBuilder.build());
  }

  private Link createProtocolLink(ScmProtocol protocol, Repository repository) {
    return Link.linkBuilder("protocol", protocol.getUrl(repository, uriInfoStore.get().getBaseUri().resolve("../.."))).withName(protocol.getType()).build();
  }
}
