package sonia.scm.api.v2.resources;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import sonia.scm.config.ScmConfiguration;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

public class GlobalConfigDtoToScmConfigurationMapperTest {

  @InjectMocks
  private GlobalConfigDtoToScmConfigurationMapperImpl mapper;

  @Test
  public void shouldMapFields() {
    GlobalConfigDto dto = createDefaultDto();
    ScmConfiguration config = mapper.map(dto);
    assertEquals("baseurl" , config.getBaseUrl());
  }

  @Before
  public void init() {
    initMocks(this);
  }

  private GlobalConfigDto createDefaultDto() {
    GlobalConfigDto globalConfigDto = new GlobalConfigDto();
    globalConfigDto.setBaseUrl("baseurl");
    return globalConfigDto;
  }
}
