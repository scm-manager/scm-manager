package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;

@Mapper
public abstract class FileObjectToFileObjectDtoMapper extends BaseFileObjectDtoMapper {

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  protected abstract FileObjectDto map(FileObject fileObject, @Context NamespaceAndName namespaceAndName, @Context BrowserResult browserResult);

  @Override
  void applyEnrichers(Links.Builder links, Embedded.Builder embeddedBuilder, NamespaceAndName namespaceAndName, BrowserResult browserResult, FileObject fileObject) {
    applyEnrichers(new EdisonHalAppender(links, embeddedBuilder), fileObject, namespaceAndName, browserResult, browserResult.getRevision());
  }
}
