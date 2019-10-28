package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.mapstruct.Mapper;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;

@Mapper
public abstract class FileObjectToFileObjectDtoMapper extends BaseFileObjectDtoMapper {

  @Override
  void applyEnrichers(Links.Builder links, Embedded.Builder embeddedBuilder, NamespaceAndName namespaceAndName, BrowserResult browserResult, FileObject fileObject) {
    applyEnrichers(new EdisonHalAppender(links, embeddedBuilder), fileObject, namespaceAndName, browserResult, browserResult.getRevision());
  }
}
