package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.SubRepository;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;

@Mapper
public abstract class FileObjectMapper extends BaseMapper<FileObject, FileObjectDto> {

  @Inject
  private ResourceLinks resourceLinks;

  protected abstract FileObjectDto map(FileObject fileObject, @Context NamespaceAndName namespaceAndName, @Context String revision);

  abstract SubRepositoryDto mapSubrepository(SubRepository subRepository);

  @AfterMapping
  void addLinks(FileObject fileObject, @MappingTarget FileObjectDto dto, @Context NamespaceAndName namespaceAndName, @Context String revision) {
      dto.add(Links.linkingTo()
        .self(resourceLinks.source().sourceWithPath(namespaceAndName.getNamespace(), namespaceAndName.getName(), revision, fileObject.getName()))
        .single(link("content", resourceLinks.source().content(namespaceAndName.getNamespace(), namespaceAndName.getName(), revision, fileObject.getName())))
        .build());
  }
}
