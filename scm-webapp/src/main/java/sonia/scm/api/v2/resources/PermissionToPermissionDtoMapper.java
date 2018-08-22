package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Permission;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class PermissionToPermissionDtoMapper {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract PermissionDto map(Permission permission, @Context NamespaceAndName namespaceAndName);

  /**
   * Add the self, update and delete links.
   *
   * @param target the mapped dto
   * @param namespaceAndName the repository namespace and name
   */
  @AfterMapping
  void appendLinks(@MappingTarget PermissionDto target, @Context NamespaceAndName namespaceAndName) {
    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.permission().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), target.getName()));
      linksBuilder.single(link("update", resourceLinks.permission().update(namespaceAndName.getNamespace(), namespaceAndName.getName(), target.getName())));
      linksBuilder.single(link("delete", resourceLinks.permission().delete(namespaceAndName.getNamespace(), namespaceAndName.getName(), target.getName())));
    target.add(linksBuilder.build());
  }
}

