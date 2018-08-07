package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.Branch;
import sonia.scm.repository.NamespaceAndName;

import javax.inject.Inject;

import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class BranchToBranchDtoMapper {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract BranchDto map(Branch branch, @Context NamespaceAndName namespaceAndName);

  @AfterMapping
  void appendLinks(@MappingTarget BranchDto target, @Context NamespaceAndName namespaceAndName) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.branch().self(namespaceAndName, target.getName()));
    target.add(linksBuilder.build());
  }
}
