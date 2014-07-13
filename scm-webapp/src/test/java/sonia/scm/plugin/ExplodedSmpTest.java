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

import com.google.common.collect.Lists;

import org.junit.Test;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class ExplodedSmpTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testCompareTo()
  {
    ExplodedSmp e1 = create("a", "c", "1", "a:a");
    ExplodedSmp e3 = create("a", "a", "1");
    ExplodedSmp e2 = create("a", "b", "1");
    List<ExplodedSmp> es = list(e1, e2, e3);

    is(es, 2, "c");
  }

  /**
   * Method description
   *
   */
  @Test(expected = PluginCircularDependencyException.class)
  public void testCompareToCyclicDependency()
  {
    ExplodedSmp e1 = create("a", "a", "1", "a:c");
    ExplodedSmp e2 = create("a", "b", "1");
    ExplodedSmp e3 = create("a", "c", "1", "a:a");

    list(e1, e2, e3);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCompareToTransitiveDependencies()
  {
    ExplodedSmp e1 = create("a", "a", "1", "a:b");
    ExplodedSmp e2 = create("a", "b", "1");
    ExplodedSmp e3 = create("a", "c", "1", "a:a");

    List<ExplodedSmp> es = list(e1, e2, e3);

    is(es, 0, "b");
    is(es, 1, "a");
    is(es, 2, "c");
  }

  /**
   * Method description
   *
   */
  @Test
  public void testMultipleDependencies()
  {
    ExplodedSmp e1 = create("a", "a", "1", "a:b", "a:c");
    ExplodedSmp e2 = create("a", "b", "1", "a:c");
    ExplodedSmp e3 = create("a", "c", "1");
    List<ExplodedSmp> es = list(e1, e2, e3);

    is(es, 2, "a");
  }

  /**
   * Method description
   *
   *
   * @param groupId
   * @param artifactId
   * @param version
   * @param dependencies
   *
   * @return
   */
  private ExplodedSmp create(String groupId, String artifactId, String version,
    String... dependencies)
  {
    return new ExplodedSmp(null, new PluginId(groupId, artifactId, version),
      Lists.newArrayList(dependencies));
  }

  /**
   * Method description
   *
   *
   * @param e
   *
   * @return
   */
  private List<ExplodedSmp> list(ExplodedSmp... e)
  {
    List<ExplodedSmp> es = Lists.newArrayList(e);

    Collections.sort(es);

    return es;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param es
   * @param p
   * @param a
   */
  private void is(List<ExplodedSmp> es, int p, String a)
  {
    assertEquals(a, es.get(p).getPluginId().getArtifactId());
  }
}
