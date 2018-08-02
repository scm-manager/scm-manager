package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.RepositoryType;

import javax.inject.Inject;

import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class RepositoryTypeToRepositoryTypeDtoMapper extends BaseMapper<RepositoryType, RepositoryTypeDto> {

  @Inject
  private ResourceLinks resourceLinks;

  @AfterMapping
  void appendLinks(RepositoryType repositoryType, @MappingTarget RepositoryTypeDto target) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.repositoryType().self(repositoryType.getName()));
    target.add(linksBuilder.build());
  }

}
