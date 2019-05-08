package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRolePermissions;

import javax.inject.Inject;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class RepositoryRoleToRepositoryRoleDtoMapper extends BaseMapper<RepositoryRole, RepositoryRoleDto> {

  @Inject
  private ResourceLinks resourceLinks;

  @Override
  @Mapping(source = "type", target = "system")
  public abstract RepositoryRoleDto map(RepositoryRole modelObject);

  @ObjectFactory
  RepositoryRoleDto createDto(RepositoryRole repositoryRole) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.repositoryRole().self(repositoryRole.getName()));
    if (!isSystemRole(repositoryRole.getType()) && RepositoryRolePermissions.modify().isPermitted()) {
      linksBuilder.single(link("delete", resourceLinks.repositoryRole().delete(repositoryRole.getName())));
      linksBuilder.single(link("update", resourceLinks.repositoryRole().update(repositoryRole.getName())));
    }

    Embedded.Builder embeddedBuilder = embeddedBuilder();
    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), repositoryRole);

    return new RepositoryRoleDto(linksBuilder.build(), embeddedBuilder.build());
  }

  boolean isSystemRole(String type) {
    return "system".equals(type);
  }
}
