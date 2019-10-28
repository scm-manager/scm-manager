package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;

@Mapper
public abstract class BrowserResultToFileObjectDtoMapper extends BaseFileObjectDtoMapper {

  FileObjectDto map(BrowserResult browserResult, @Context NamespaceAndName namespaceAndName) {
    FileObjectDto fileObjectDto = map(browserResult.getFile(), namespaceAndName, browserResult);
    fileObjectDto.setRevision(browserResult.getRevision());
    return fileObjectDto;
  }

  @Override
  void applyEnrichers(Links.Builder links, Embedded.Builder embeddedBuilder, NamespaceAndName namespaceAndName, BrowserResult browserResult, FileObject fileObject) {
    applyEnrichers(new EdisonHalAppender(links, embeddedBuilder), browserResult, namespaceAndName);
  }
}
