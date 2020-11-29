/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.api.v2.resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.HgConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class HgConfigDtoToHgConfigMapperTest {

  @InjectMocks
  private HgConfigDtoToHgConfigMapperImpl mapper;

  @Test
  public void shouldMapFields() {
    HgConfigDto dto = createDefaultDto();
    HgConfig config = mapper.map(dto);

    assertTrue(config.isDisabled());

    assertEquals("ABC", config.getEncoding());
    assertEquals("/etc/hg", config.getHgBinary());
    assertTrue(config.isShowRevisionInId());
    assertTrue(config.isEnableHttpPostArgs());
  }

  private HgConfigDto createDefaultDto() {
    HgConfigDto configDto = new HgConfigDto();
    configDto.setDisabled(true);
    configDto.setEncoding("ABC");
    configDto.setHgBinary("/etc/hg");
    configDto.setShowRevisionInId(true);
    configDto.setEnableHttpPostArgs(true);

    return configDto;
  }
}
