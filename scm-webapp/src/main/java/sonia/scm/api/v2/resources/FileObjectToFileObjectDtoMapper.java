package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.SubRepository;

import javax.inject.Inject;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;

@Mapper
public abstract class FileObjectToFileObjectDtoMapper extends HalAppenderMapper implements InstantAttributeMapper {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  protected abstract FileObjectDto map(FileObject fileObject, @Context NamespaceAndName namespaceAndName, @Context String revision);

  abstract SubRepositoryDto mapSubrepository(SubRepository subRepository);

  @ObjectFactory
  FileObjectDto createDto(@Context NamespaceAndName namespaceAndName, @Context String revision, FileObject fileObject) {
    String path = removeFirstSlash(fileObject.getPath());
    Links.Builder links = Links.linkingTo();
    if (fileObject.isDirectory()) {
      links.self(resourceLinks.source().sourceWithPath(namespaceAndName.getNamespace(), namespaceAndName.getName(), revision, path));
    } else {
      links.self(resourceLinks.source().content(namespaceAndName.getNamespace(), namespaceAndName.getName(), revision, path));
      links.single(link("history", resourceLinks.fileHistory().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), revision, path)));
    }

    Embedded.Builder embeddedBuilder = embeddedBuilder();
    applyEnrichers(new EdisonHalAppender(links, embeddedBuilder), fileObject, namespaceAndName, revision);

    return new FileObjectDto(links.build(), embeddedBuilder.build());
  }

  private String removeFirstSlash(String source) {
    return source.startsWith("/") ? source.substring(1) : source;
  }
}
