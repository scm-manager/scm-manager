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

import sonia.scm.installer.HgPackage;
import sonia.scm.repository.HgConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

class HgConfigTests {

  private HgConfigTests() {
  }

  static HgConfig createConfiguration() {
    HgConfig config = new HgConfig();
    config.setDisabled(true);

    config.setEncoding("ABC");
    config.setHgBinary("/etc/hg");
    config.setPythonBinary("/py");
    config.setPythonPath("/etc/");
    config.setShowRevisionInId(true);
    config.setUseOptimizedBytecode(true);

    return config;
  }

  static void assertEqualsConfiguration(HgConfigDto dto) {
    assertTrue(dto.isDisabled());

    assertEquals("ABC", dto.getEncoding());
    assertEquals("/etc/hg", dto.getHgBinary());
    assertEquals("/py", dto.getPythonBinary());
    assertEquals("/etc/", dto.getPythonPath());
    assertTrue(dto.isShowRevisionInId());
    assertTrue(dto.isUseOptimizedBytecode());
  }

  static HgPackage createPackage() {
    HgPackage hgPackage= new HgPackage();
    hgPackage.setArch("arch");
    hgPackage.setId("1");
    hgPackage.setHgVersion("2");
    hgPackage.setPlatform("someOs");
    hgPackage.setPythonVersion("3");
    hgPackage.setSize(4);
    hgPackage.setUrl("https://package");
    hgPackage.setHgConfigTemplate(createConfiguration());
    return hgPackage;
  }

  static void assertEqualsPackage(HgConfigPackagesDto.HgConfigPackageDto dto) {
    assertEquals("arch", dto.getArch());
    assertEquals("1", dto.getId());
    assertEquals("2", dto.getHgVersion());
    assertEquals("someOs", dto.getPlatform());
    assertEquals("3", dto.getPythonVersion());
    assertEquals(4, dto.getSize());
    assertEquals("https://package", dto.getUrl());

    assertEqualsConfiguration(dto.getHgConfigTemplate());
    assertTrue(dto.getHgConfigTemplate().getLinks().isEmpty());
  }

}
