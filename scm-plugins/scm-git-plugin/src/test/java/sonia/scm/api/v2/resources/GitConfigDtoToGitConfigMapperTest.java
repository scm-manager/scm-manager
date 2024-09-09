/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.api.v2.resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.GitConfig;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class GitConfigDtoToGitConfigMapperTest {

  @InjectMocks
  private GitConfigDtoToGitConfigMapperImpl mapper;

  @Test
  public void shouldMapFields() {
    GitConfigDto dto = createDefaultDto();
    GitConfig config = mapper.map(dto);
    assertEquals("express", config.getGcExpression());
    assertFalse(config.isDisabled());
    assertTrue(config.isNonFastForwardDisallowed());
  }

  private GitConfigDto createDefaultDto() {
    GitConfigDto gitConfigDto = new GitConfigDto();
    gitConfigDto.setGcExpression("express");
    gitConfigDto.setDisabled(false);
    gitConfigDto.setNonFastForwardDisallowed(true);
    return gitConfigDto;
  }
}
