package sonia.scm.api.v2.resources;

import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.NamespaceAndName;

import javax.inject.Inject;

public class BrowserResultToFileObjectDtoMapper {

  private final FileObjectToFileObjectDtoMapper fileObjectToFileObjectDtoMapper;

  @Inject
  public BrowserResultToFileObjectDtoMapper(FileObjectToFileObjectDtoMapper fileObjectToFileObjectDtoMapper) {
    this.fileObjectToFileObjectDtoMapper = fileObjectToFileObjectDtoMapper;
  }

  public FileObjectDto map(BrowserResult browserResult, NamespaceAndName namespaceAndName, String path) {
    FileObjectDto fileObjectDto = fileObjectToFileObjectDtoMapper.map(browserResult.getFile(), namespaceAndName, browserResult.getRevision());
    fileObjectDto.setRevision( browserResult.getRevision() );
    return fileObjectDto;
  }
}
