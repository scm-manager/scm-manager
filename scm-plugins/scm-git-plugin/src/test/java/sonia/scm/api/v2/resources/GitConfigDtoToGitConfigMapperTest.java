package sonia.scm.api.v2.resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.repository.GitConfig;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.class)
public class GitConfigDtoToGitConfigMapperTest {

  @InjectMocks
  private GitConfigDtoToGitConfigMapperImpl mapper;

  @Test
  public void shouldMapFields() {
    GitConfigDto dto = createDefaultDto();
    GitConfig config = mapper.map(dto);
    assertEquals("express", config.getGcExpression());
    assertEquals("repository/directory", config.getRepositoryDirectory().getPath());
    assertFalse(config.isDisabled());
  }

  private GitConfigDto createDefaultDto() {
    GitConfigDto gitConfigDto = new GitConfigDto();
    gitConfigDto.setGcExpression("express");
    gitConfigDto.setDisabled(false);
    gitConfigDto.setRepositoryDirectory(new File("repository/directory"));
    return gitConfigDto;
  }
}
