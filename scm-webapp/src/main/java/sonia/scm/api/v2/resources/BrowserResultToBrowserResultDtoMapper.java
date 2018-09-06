package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class BrowserResultToBrowserResultDtoMapper {

  @Inject
  private FileObjectToFileObjectDtoMapper fileObjectToFileObjectDtoMapper;

  @Inject
  private ResourceLinks resourceLinks;

  public BrowserResultDto map(BrowserResult browserResult, NamespaceAndName namespaceAndName) {
    BrowserResultDto browserResultDto = new BrowserResultDto();

    browserResultDto.setTag(browserResult.getTag());
    browserResultDto.setBranch(browserResult.getBranch());
    browserResultDto.setRevision(browserResult.getRevision());

    List<FileObjectDto> fileObjectDtoList = new ArrayList<>();
    for (FileObject fileObject : browserResult.getFiles()) {
      fileObjectDtoList.add(mapFileObject(fileObject, namespaceAndName, browserResult.getRevision()));
    }

    browserResultDto.setFiles(fileObjectDtoList);
    this.addLinks(browserResult, browserResultDto, namespaceAndName);
    return browserResultDto;
  }

  private FileObjectDto mapFileObject(FileObject fileObject, NamespaceAndName namespaceAndName, String revision) {
    return fileObjectToFileObjectDtoMapper.map(fileObject, namespaceAndName, revision);
  }

  private void addLinks(BrowserResult browserResult, BrowserResultDto dto, NamespaceAndName namespaceAndName) {
    if (browserResult.getRevision() == null) {
      dto.add(Links.linkingTo().self(resourceLinks.source().selfWithoutRevision(namespaceAndName.getNamespace(), namespaceAndName.getName())).build());
    } else {
      dto.add(Links.linkingTo().self(resourceLinks.source().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), browserResult.getRevision())).build());
    }
  }


}
