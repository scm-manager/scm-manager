package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import java.util.List;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.stream.Collectors.toList;

public class PermissionCollectionToDtoMapper {

  private final ResourceLinks resourceLinks;
  private final PermissionToPermissionDtoMapper permissionToPermissionDtoMapper;

  @Inject
  public PermissionCollectionToDtoMapper(PermissionToPermissionDtoMapper permissionToPermissionDtoMapper, ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
    this.permissionToPermissionDtoMapper = permissionToPermissionDtoMapper;
  }

  public HalRepresentation map(Repository repository) {
    List<PermissionDto> permissionDtoList = repository.getPermissions()
      .stream()
      .map(permission -> permissionToPermissionDtoMapper.map(permission, repository))
      .collect(toList());
    return new HalRepresentation(createLinks(repository), embedDtos(permissionDtoList));
  }

  private Links createLinks(Repository repository) {
    RepositoryPermissions.permissionRead(repository).check();
    Links.Builder linksBuilder = linkingTo()
      .with(Links.linkingTo().self(resourceLinks.permission().all(repository.getNamespace(), repository.getName())).build());
    if (RepositoryPermissions.permissionWrite(repository).isPermitted()) {
      linksBuilder.single(link("create", resourceLinks.permission().create(repository.getNamespace(), repository.getName())));
    }
    return linksBuilder.build();
  }

  private Embedded embedDtos(List<PermissionDto> permissionDtoList) {
    return embeddedBuilder()
      .with("permissions", permissionDtoList)
      .build();
  }
}
