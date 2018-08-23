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
import java.net.URI;

import static de.otto.edison.hal.Link.link;

@Mapper
public abstract class FileObjectMapper extends BaseMapper<FileObject, FileObjectDto> {

  @Inject
  private ResourceLinks resourceLinks;

  protected abstract FileObjectDto map(FileObject fileObject, @Context NamespaceAndName namespaceAndName, @Context String revision);

  abstract SubRepositoryDto mapSubrepository(SubRepository subRepository);

  @AfterMapping
  void addLinks(FileObject fileObject, @MappingTarget FileObjectDto dto, @Context NamespaceAndName namespaceAndName, @Context String revision) {
    String path = fileObject.getPath();
    Links.Builder links = Links.linkingTo()
      .self(addPath(resourceLinks.source().sourceWithPath(namespaceAndName.getNamespace(), namespaceAndName.getName(), revision, ""), path));
    if (!dto.isDirectory()) {
      links.single(link("content", addPath(resourceLinks.source().content(namespaceAndName.getNamespace(), namespaceAndName.getName(), revision, ""), path)));
    }

    dto.add(links.build());
  }

  // we have to add the file path using URI, so that path separators (aka '/') will not be encoded as '%2F'
  private String addPath(String sourceWithPath, String path) {
    return URI.create(sourceWithPath).resolve(path).toASCIIString();
  }
}
