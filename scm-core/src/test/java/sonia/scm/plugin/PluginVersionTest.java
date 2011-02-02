/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;

/**
 *
 * @author Sebastian Sdorra
 */
public class PluginVersionTest
{

  /**
   * Method description
   *
   */
  @Test
  public void parseSimpleVersion()
  {
    PluginVersion v = new PluginVersion("1.0");

    assertTrue(v.getMajor() == 1);
    assertTrue(v.getMinor() == 0);
    assertTrue(v.getMaintenance() == 0);
    assertFalse(v.isSnapshot());
    assertTrue(v.getType() == PluginVersionType.RELEASE);
    assertEquals(v.getParsedVersion(), "1.0.0");

    // test with snapshot
    v = new PluginVersion("1.1-SNAPSHOT");
    assertTrue(v.getMajor() == 1);
    assertTrue(v.getMinor() == 1);
    assertTrue(v.getMaintenance() == 0);
    assertTrue(v.isSnapshot());
    assertTrue(v.getType() == PluginVersionType.RELEASE);
    assertEquals(v.getParsedVersion(), "1.1.0-SNAPSHOT");

    // test with maintenance
    v = new PluginVersion("2.3.14");
    assertTrue(v.getMajor() == 2);
    assertTrue(v.getMinor() == 3);
    assertTrue(v.getMaintenance() == 14);
    assertFalse(v.isSnapshot());
    assertTrue(v.getType() == PluginVersionType.RELEASE);
    assertEquals(v.getParsedVersion(), "2.3.14");
  }

  /**
   * Method description
   *
   */
  @Test
  public void parseTypeVersions()
  {
    PluginVersion v = new PluginVersion("1.0-alpha");

    assertTrue(v.getMajor() == 1);
    assertTrue(v.getMinor() == 0);
    assertTrue(v.getMaintenance() == 0);
    assertFalse(v.isSnapshot());
    assertTrue(v.getType() == PluginVersionType.ALPHA);
    assertTrue(v.getTypeVersion() == 1);
    assertEquals(v.getParsedVersion(), "1.0.0-alpha1");

    // Test release candidate
    v = new PluginVersion("2.1.2-RC3");
    assertTrue(v.getMajor() == 2);
    assertTrue(v.getMinor() == 1);
    assertTrue(v.getMaintenance() == 2);
    assertFalse(v.isSnapshot());
    assertTrue(v.getType() == PluginVersionType.RELEASE_CANDIDAT);
    assertTrue(v.getTypeVersion() == 3);
    assertEquals(v.getParsedVersion(), "2.1.2-RC3");
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCompareTo()
  {
    PluginVersion[] versions = new PluginVersion[9];

    versions[0] = PluginVersion.createVersion("2.3.1-SNAPSHOT");
    versions[1] = PluginVersion.createVersion("2.3.1-beta1");
    versions[2] = PluginVersion.createVersion("2.3.1-beta2");
    versions[3] = PluginVersion.createVersion("2.3.1-M1");
    versions[4] = PluginVersion.createVersion("2.3.1-alpha2");
    versions[5] = PluginVersion.createVersion("2.3.1-RC1");
    versions[6] = PluginVersion.createVersion("2.3.1");
    versions[7] = PluginVersion.createVersion("2.3");
    versions[8] = PluginVersion.createVersion("2.4.6");
    Arrays.sort(versions);
    assertEquals(versions[0].getParsedVersion(), "2.4.6");
    assertEquals(versions[1].getParsedVersion(), "2.3.1");
    assertEquals(versions[2].getParsedVersion(), "2.3.1-SNAPSHOT");
    assertEquals(versions[3].getParsedVersion(), "2.3.1-RC1");
    assertEquals(versions[4].getParsedVersion(), "2.3.1-beta2");
    assertEquals(versions[5].getParsedVersion(), "2.3.1-beta1");
    assertEquals(versions[6].getParsedVersion(), "2.3.1-alpha2");
    assertEquals(versions[7].getParsedVersion(), "2.3.1-M1");
    assertEquals(versions[8].getParsedVersion(), "2.3.0");
  }

  /**
   * Method description
   *
   */
  @Test
  public void testIsNewer()
  {
    assertFalse(PluginVersion.createVersion("1.0").isNewer("1.0.1"));
    assertTrue(PluginVersion.createVersion("1.1").isNewer("1.1-alpha1"));
    assertTrue(PluginVersion.createVersion("1.1").isNewer("1.1-RC5"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testIsOlder()
  {
    assertFalse(PluginVersion.createVersion("1.0.1").isOlder("1.0"));
    assertTrue(PluginVersion.createVersion("1.1-alpha1").isOlder("1.1"));
    assertTrue(PluginVersion.createVersion("1.1-RC5").isOlder("1.1"));
  }
}
