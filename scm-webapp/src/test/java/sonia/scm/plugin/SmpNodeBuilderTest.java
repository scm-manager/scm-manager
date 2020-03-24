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
