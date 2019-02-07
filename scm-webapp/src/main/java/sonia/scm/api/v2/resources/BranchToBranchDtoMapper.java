package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.Branch;
import sonia.scm.repository.NamespaceAndName;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.linkBuilder;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class BranchToBranchDtoMapper extends HalAppenderMapper {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract BranchDto map(Branch branch, @Context NamespaceAndName namespaceAndName);

  @ObjectFactory
  BranchDto createDto(@Context NamespaceAndName namespaceAndName, Branch branch) {
    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.branch().self(namespaceAndName, branch.getName()))
      .single(linkBuilder("history", resourceLinks.branch().history(namespaceAndName, branch.getName())).build())
      .single(linkBuilder("changeset", resourceLinks.changeset().changeset(namespaceAndName.getNamespace(), namespaceAndName.getName(), branch.getRevision())).build())
      .single(linkBuilder("source", resourceLinks.source().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), branch.getRevision())).build());

    Embedded.Builder embeddedBuilder = Embedded.embeddedBuilder();
    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), branch, namespaceAndName);

    return new BranchDto(linksBuilder.build(), embeddedBuilder.build());
  }
}
