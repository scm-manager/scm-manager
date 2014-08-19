/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class PluginTreeTest
{

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test(expected = PluginConditionFailedException.class)
  public void testPluginConditionFailed() throws IOException
  {
    PluginCondition condition = new PluginCondition("999",
                                  new ArrayList<String>(), "hit");
    Plugin plugin = new Plugin(createInfo("a", "b", "1"), null, condition,
                      null);
    ExplodedSmp smp = createSmp(plugin);

    new PluginTree(smp).getRootNodes();
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test(expected = PluginNotInstalledException.class)
  public void testPluginNotInstalled() throws IOException
  {
    new PluginTree(createSmpWithDependency("b", "a")).getRootNodes();
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testRootNotes() throws IOException
  {
    List<ExplodedSmp> smps = createSmps("a", "b", "c");
    List<String> nodes = unwrapIds(new PluginTree(smps).getRootNodes());

    assertThat(nodes, containsInAnyOrder("a:a", "b:b", "c:c"));
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testSimpleDependencies() throws IOException
  {
    //J-
    ExplodedSmp[] smps = new ExplodedSmp[] { 
      createSmpWithDependency("a"),
      createSmpWithDependency("b", "a"),
      createSmpWithDependency("c", "a", "b") 
    };
    //J+

    PluginTree tree = new PluginTree(smps);
    List<PluginNode> rootNodes = tree.getRootNodes();

    assertThat(unwrapIds(rootNodes), containsInAnyOrder("a:a"));

    PluginNode a = rootNodes.get(0);

    assertThat(unwrapIds(a.getChildren()), containsInAnyOrder("b:b", "c:c"));

    PluginNode b = a.getChild("b:b");

    assertThat(unwrapIds(b.getChildren()), containsInAnyOrder("c:c"));
  }

  /**
   * Method description
   *
   *
   * @param groupId
   * @param artifactId
   * @param version
   *
   * @return
   */
  private PluginInformation createInfo(String groupId, String artifactId,
    String version)
  {
    PluginInformation info = new PluginInformation();

    info.setGroupId(groupId);
    info.setArtifactId(artifactId);
    info.setVersion(version);

    return info;
  }

  /**
   * Method description
   *
   *
   * @param plugin
   *
   * @return
   *
   * @throws IOException
   */
  private ExplodedSmp createSmp(Plugin plugin) throws IOException
  {
    return new ExplodedSmp(tempFolder.newFile().toPath(), plugin);
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   *
   * @throws IOException
   */
  private ExplodedSmp createSmp(String name) throws IOException
  {
    return createSmp(new Plugin(createInfo(name, name, "1.0.0"), null, null,
      null));
  }

  /**
   * Method description
   *
   *
   * @param name
   * @param dependencies
   *
   * @return
   *
   * @throws IOException
   */
  private ExplodedSmp createSmpWithDependency(String name,
    String... dependencies)
    throws IOException
  {
    Set<String> dependencySet = new HashSet<>();

    for (String d : dependencies)
    {
      dependencySet.add(d.concat(":").concat(d));
    }

    Plugin plugin = new Plugin(createInfo(name, name, "1"), null, null,
                      dependencySet);

    return createSmp(plugin);
  }

  /**
   * Method description
   *
   *
   * @param names
   *
   * @return
   *
   * @throws IOException
   */
  private List<ExplodedSmp> createSmps(String... names) throws IOException
  {
    List<ExplodedSmp> smps = Lists.newArrayList();

    for (String name : names)
    {
      smps.add(createSmp(name));
    }

    return smps;
  }

  /**
   * Method description
   *
   *
   * @param nodes
   *
   * @return
   */
  private List<String> unwrapIds(List<PluginNode> nodes)
  {
    return Lists.transform(nodes, new Function<PluginNode, String>()
    {

      @Override
      public String apply(PluginNode input)
      {
        return input.getId();
      }
    });
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
}
