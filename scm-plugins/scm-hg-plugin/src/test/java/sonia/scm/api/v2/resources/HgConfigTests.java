package sonia.scm.api.v2.resources;

import sonia.scm.installer.HgPackage;
import sonia.scm.repository.HgConfig;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

class HgConfigTests {

  private HgConfigTests() {
  }

  static HgConfig createConfiguration() {
    HgConfig config = new HgConfig();
    config.setDisabled(true);
    config.setRepositoryDirectory(new File("repository/directory"));

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
    assertEquals("repository/directory", dto.getRepositoryDirectory().getPath());

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
