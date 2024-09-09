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
import sonia.scm.repository.HgGlobalConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class HgGlobalConfigDtoToHgGlobalConfigMapperTest {

  @InjectMocks
  private HgGlobalConfigDtoToHgConfigMapperImpl mapper;

  @Test
  public void shouldMapFields() {
    HgGlobalGlobalConfigDto dto = createDefaultDto();
    HgGlobalConfig config = mapper.map(dto);

    assertTrue(config.isDisabled());

    assertEquals("ABC", config.getEncoding());
    assertEquals("/etc/hg", config.getHgBinary());
    assertTrue(config.isShowRevisionInId());
    assertTrue(config.isEnableHttpPostArgs());
  }

  private HgGlobalGlobalConfigDto createDefaultDto() {
    HgGlobalGlobalConfigDto configDto = new HgGlobalGlobalConfigDto();
    configDto.setDisabled(true);
    configDto.setEncoding("ABC");
    configDto.setHgBinary("/etc/hg");
    configDto.setShowRevisionInId(true);
    configDto.setEnableHttpPostArgs(true);

    return configDto;
  }
}
