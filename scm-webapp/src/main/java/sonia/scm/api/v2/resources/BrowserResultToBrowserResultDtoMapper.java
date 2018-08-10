package sonia.scm.api.v2.resources;

import org.mapstruct.Mapper;
import sonia.scm.repository.BrowserResult;

@Mapper
public abstract class BrowserResultToBrowserResultDtoMapper extends BaseMapper<BrowserResult, BrowserResultDto> {

}
