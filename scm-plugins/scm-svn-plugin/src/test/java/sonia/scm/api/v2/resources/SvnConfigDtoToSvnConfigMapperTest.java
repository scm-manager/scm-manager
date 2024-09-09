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
import sonia.scm.repository.Compatibility;
import sonia.scm.repository.SvnConfig;

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

    assertEquals(Compatibility.PRE15, config.getCompatibility());
    assertTrue(config.isEnabledGZip());
  }

  private SvnConfigDto createDefaultDto() {
    SvnConfigDto configDto = new SvnConfigDto();
    configDto.setDisabled(true);
    configDto.setCompatibility(Compatibility.PRE15);
    configDto.setEnabledGZip(true);

    return configDto;
  }
}
