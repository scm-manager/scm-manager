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
public abstract class FileObjectToFileObjectDtoMapper extends BaseMapper<FileObject, FileObjectDto> {

  @Inject
  private ResourceLinks resourceLinks;

  protected abstract FileObjectDto map(FileObject fileObject, @Context NamespaceAndName namespaceAndName, @Context String revision);

  abstract SubRepositoryDto mapSubrepository(SubRepository subRepository);

  @AfterMapping
  void addLinks(FileObject fileObject, @MappingTarget FileObjectDto dto, @Context NamespaceAndName namespaceAndName, @Context String revision) {
    String path = removeFirstSlash(fileObject.getPath());
    Links.Builder links = Links.linkingTo();
    if (dto.isDirectory()) {
      links.self(resourceLinks.source().sourceWithPath(namespaceAndName.getNamespace(), namespaceAndName.getName(), revision, path));
    } else {
      links.self(resourceLinks.source().content(namespaceAndName.getNamespace(), namespaceAndName.getName(), revision, path));
      links.single(link("history", resourceLinks.fileHistory().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), revision, path)));
    }

    dto.add(links.build());
  }

  private String removeFirstSlash(String source) {
    return source.startsWith("/") ? source.substring(1) : source;
  }
}
