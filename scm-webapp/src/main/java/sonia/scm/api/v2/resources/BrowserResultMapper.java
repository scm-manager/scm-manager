package sonia.scm.api.v2.resources;

import org.mapstruct.Mapper;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;

import java.util.ArrayList;
import java.util.List;

@Mapper
public abstract class BrowserResultMapper extends BaseMapper<BrowserResult, BrowserResultDto> {

  abstract FileObjectDto mapFileObject(FileObject fileObject);

  public BrowserResultDto map(BrowserResult browserResult) {
    BrowserResultDto browserResultDto = new BrowserResultDto();

    browserResultDto.setTag(browserResult.getTag());
    browserResultDto.setBranch(browserResult.getBranch());
    browserResultDto.setRevision(browserResult.getRevision());

    List<FileObjectDto> fileObjectDtoList = new ArrayList<>();
    for (FileObject fileObject : browserResult.getFiles()) {
      fileObjectDtoList.add(mapFileObject(fileObject));
    }

    browserResultDto.setFiles(fileObjectDtoList);

    return browserResultDto;
  }
}
