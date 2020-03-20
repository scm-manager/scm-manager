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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.installer.HgPackage;
import sonia.scm.installer.HgPackages;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static sonia.scm.api.v2.resources.HgConfigTests.assertEqualsPackage;
import static sonia.scm.api.v2.resources.HgConfigTests.createPackage;

@RunWith(MockitoJUnitRunner.class)
public class HgConfigPackagesToDtoMapperTest {

  private URI baseUri = URI.create("http://example.com/base/");

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ScmPathInfoStore scmPathInfoStore;

  @InjectMocks
  private HgConfigPackagesToDtoMapperImpl mapper;

  private URI expectedBaseUri;

  @Before
  public void init() {
    when(scmPathInfoStore.get().getApiRestUri()).thenReturn(baseUri);
    expectedBaseUri = baseUri.resolve(HgConfigResource.HG_CONFIG_PATH_V2 + "/packages");
  }

  @Test
  public void shouldMapFields() {
    HgPackages hgPackages = new HgPackages();
    hgPackages.setPackages(createPackages());

    HgConfigPackagesDto dto = mapper.map(hgPackages);

    assertThat(dto.getPackages()).hasSize(2);

    HgConfigPackagesDto.HgConfigPackageDto hgPackageDto1 = dto.getPackages().get(0);
    assertEqualsPackage(hgPackageDto1);

    HgConfigPackagesDto.HgConfigPackageDto hgPackageDto2 = dto.getPackages().get(1);
    // Just verify a random field
    assertThat(hgPackageDto2.getId()).isNull();

    assertEquals(expectedBaseUri.toString(), dto.getLinks().getLinkBy("self").get().getHref());
  }


  private List<HgPackage> createPackages() {
    return Arrays.asList(createPackage(), new HgPackage());
  }

}
