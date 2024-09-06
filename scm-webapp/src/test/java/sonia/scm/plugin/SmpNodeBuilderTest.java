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

package sonia.scm.plugin;

import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.of;
import static org.assertj.core.api.Assertions.assertThat;

public class SmpNodeBuilderTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void onePluginOnly() throws IOException {
    ExplodedSmp[] smps = new ExplodedSmp[] {
      createSmpWithDependency("scm-ssh-plugin")
    };

    List<PluginNode> nodes = new SmpNodeBuilder().buildNodeTree(smps);

    assertThat(nodes).extracting("id").contains("scm-ssh-plugin");
  }

  @Test
  public void simpleDependency() throws IOException {
    ExplodedSmp[] smps = new ExplodedSmp[]{
      createSmpWithDependency("scm-editor-plugin"),
      createSmpWithDependency("scm-pathwp-plugin", "scm-editor-plugin")
    };

    List<PluginNode> nodes = new SmpNodeBuilder().buildNodeTree(smps);

    assertThat(nodes).extracting("id").containsExactlyInAnyOrder("scm-editor-plugin", "scm-pathwp-plugin");
    assertThat(findNode(nodes, "scm-editor-plugin").getChildren()).extracting("id").contains("scm-pathwp-plugin");
  }

  @Test(expected = PluginNotInstalledException.class)
  public void shouldFailForUnfulfilledDependency() throws IOException {
    ExplodedSmp[] smps = new ExplodedSmp[]{
      createSmpWithDependency("scm-pathwp-plugin", "scm-editor-plugin")
    };

    new SmpNodeBuilder().buildNodeTree(smps);
  }

  @Test
  public void shouldNotFailForUnfulfilledOptionalDependency() throws IOException {
    ExplodedSmp[] smps = new ExplodedSmp[]{
      createSmpWithDependency("scm-pathwp-plugin", of(), of("scm-editor-plugin"))
    };

    new SmpNodeBuilder().buildNodeTree(smps);
  }

  @Test(expected = PluginCircularDependencyException.class)
  public void shouldFailForCircularDependency() throws IOException {
    ExplodedSmp[] smps = new ExplodedSmp[]{
      createSmpWithDependency("scm-pathwp-plugin", "scm-editor-plugin"),
      createSmpWithDependency("scm-editor-plugin", "scm-review-plugin"),
      createSmpWithDependency("scm-review-plugin", "scm-pathwp-plugin")
    };

    new SmpNodeBuilder().buildNodeTree(smps);
  }

  private PluginNode findNode(List<PluginNode> nodes, String id) {
    return nodes.stream().filter(node -> node.getId().equals(id)).findAny().orElseThrow(() -> new AssumptionViolatedException("node not found"));
  }

  private ExplodedSmp createSmp(InstalledPluginDescriptor plugin) throws IOException
  {
    return new ExplodedSmp(tempFolder.newFile().toPath(), plugin);
  }

  private PluginInformation createInfo(String name, String version) {
    PluginInformation info = new PluginInformation();

    info.setName(name);
    info.setVersion(version);

    return info;
  }

  private ExplodedSmp createSmpWithDependency(String name,
                                              String... dependencies)
    throws IOException
  {
    Set<String> dependencySet = new HashSet<>();

    for (String d : dependencies)
    {
      dependencySet.add(d);
    }
    return createSmpWithDependency(name, dependencySet, null);
  }

  private ExplodedSmp createSmpWithDependency(String name, Set<String> dependencies, Set<String> optionalDependencies) throws IOException {
    InstalledPluginDescriptor plugin = new InstalledPluginDescriptor(
      2,
      createInfo(name, "1"),
      null,
      null,
      false,
      dependencies,
      optionalDependencies
    );
    return createSmp(plugin);
  }
}
