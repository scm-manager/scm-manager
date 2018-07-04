package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.HealthCheckFailure;
import sonia.scm.repository.Permission;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class RepositoryToRepositoryDtoMapper extends BaseMapper<Repository, RepositoryDto> {

  @Inject
  private ResourceLinks resourceLinks;

  abstract HealthCheckFailureDto toDto(HealthCheckFailure failure);

  abstract PermissionDto toDto(Permission permission);

  @AfterMapping
  void appendLinks(Repository repository, @MappingTarget RepositoryDto target) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.repository().self(target.getNamespace(), target.getName()));
    if (RepositoryPermissions.delete(repository).isPermitted()) {
      linksBuilder.single(link("delete", resourceLinks.repository().delete(target.getNamespace(), target.getName())));
    }
    if (RepositoryPermissions.modify(repository).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.repository().update(target.getNamespace(), target.getName())));
    }
    linksBuilder.single(link("tags", resourceLinks.tagCollection().self(target.getNamespace(), target.getName())));
    linksBuilder.single(link("branches", resourceLinks.branchCollection().self(target.getNamespace(), target.getName())));
    linksBuilder.single(link("changesets", resourceLinks.changesetCollection().self(target.getNamespace(), target.getName())));
    linksBuilder.single(link("sources", resourceLinks.sourceCollection().self(target.getNamespace(), target.getName())));
    target.add(linksBuilder.build());
  }
}
