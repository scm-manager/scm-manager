package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import javax.inject.Inject;
import java.util.Optional;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static sonia.scm.api.v2.resources.RepositoryPermissionDto.GROUP_PREFIX;

@Mapper
public abstract class RepositoryPermissionToRepositoryPermissionDtoMapper {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract RepositoryPermissionDto map(RepositoryPermission permission, @Context Repository repository);


  @BeforeMapping
  void validatePermissions(@Context Repository repository) {
    RepositoryPermissions.permissionRead(repository).check();
  }

  /**
   * Add the self, update and delete links.
   *
   * @param target     the mapped dto
   * @param repository the repository
   */
  @AfterMapping
  void appendLinks(@MappingTarget RepositoryPermissionDto target, @Context Repository repository) {
    String permissionName = getUrlPermissionName(target);
    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.repositoryPermission().self(repository.getNamespace(), repository.getName(), permissionName));
    if (RepositoryPermissions.permissionWrite(repository).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.repositoryPermission().update(repository.getNamespace(), repository.getName(), permissionName)));
      linksBuilder.single(link("delete", resourceLinks.repositoryPermission().delete(repository.getNamespace(), repository.getName(), permissionName)));
    }
    target.add(linksBuilder.build());
  }

  public String getUrlPermissionName(RepositoryPermissionDto repositoryPermissionDto) {
    return Optional.of(repositoryPermissionDto.getName())
      .filter(p -> !repositoryPermissionDto.isGroupPermission())
      .orElse(GROUP_PREFIX + repositoryPermissionDto.getName());
  }
}
