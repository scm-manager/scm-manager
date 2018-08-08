package sonia.scm.api.v2.resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.repository.Compatibility;
import sonia.scm.repository.SvnConfig;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SvnConfigDtoToSvnConfigMapperTest {

  @InjectMocks
  private SvnConfigDtoToSvnConfigMapperImpl mapper;

  @Test
  public void shouldMapFields() {
    SvnConfigDto dto = createDefaultDto();
    SvnConfig config = mapper.map(dto);

    assertTrue(config.isDisabled());
    assertEquals("repository/directory", config.getRepositoryDirectory().getPath());

    assertEquals(Compatibility.PRE15, config.getCompatibility());
    assertTrue(config.isEnabledGZip());
  }

  private SvnConfigDto createDefaultDto() {
    SvnConfigDto configDto = new SvnConfigDto();
    configDto.setDisabled(true);
    configDto.setRepositoryDirectory(new File("repository/directory"));
    configDto.setCompatibility(Compatibility.PRE15);
    configDto.setEnabledGZip(true);

    return configDto;
  }
}
