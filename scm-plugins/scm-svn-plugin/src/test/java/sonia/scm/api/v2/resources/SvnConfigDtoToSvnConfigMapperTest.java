/**
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
