package sonia.scm.api.v2.resources;

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.mapstruct.Context;
import org.mapstruct.MapperConfig;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.SubRepository;

import javax.inject.Inject;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;

@MapperConfig
abstract class BaseFileObjectDtoMapper extends HalAppenderMapper implements InstantAttributeMapper {

  @Inject
  private ResourceLinks resourceLinks;

  @VisibleForTesting
  void setResourceLinks(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }

  abstract SubRepositoryDto mapSubrepository(SubRepository subRepository);

  @ObjectFactory
  FileObjectDto createDto(@Context NamespaceAndName namespaceAndName, @Context BrowserResult browserResult, FileObject fileObject) {
    String path = removeFirstSlash(fileObject.getPath());
    Links.Builder links = Links.linkingTo();
    if (fileObject.isDirectory()) {
      links.self(resourceLinks.source().sourceWithPath(namespaceAndName.getNamespace(), namespaceAndName.getName(), browserResult.getRevision(), path));
    } else {
      links.self(resourceLinks.source().content(namespaceAndName.getNamespace(), namespaceAndName.getName(), browserResult.getRevision(), path));
      links.single(link("history", resourceLinks.fileHistory().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), browserResult.getRevision(), path)));
    }

    Embedded.Builder embeddedBuilder = embeddedBuilder();
    applyEnrichers(links, embeddedBuilder, namespaceAndName, browserResult, fileObject);

    return new FileObjectDto(links.build(), embeddedBuilder.build());
  }

  abstract void applyEnrichers(Links.Builder links, Embedded.Builder embeddedBuilder, NamespaceAndName namespaceAndName, BrowserResult browserResult, FileObject fileObject);

  private String removeFirstSlash(String source) {
    return source.startsWith("/") ? source.substring(1) : source;
  }

}
