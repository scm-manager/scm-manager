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

package sonia.scm.version;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;

/**
 *
 * @author Sebastian Sdorra
 */
public class VersionTest
{

  /**
   * Method description
   *
   */
  @Test
  public void parseSimpleVersion()
  {
    Version v = Version.parse("1.0");

    assertTrue(v.getMajor() == 1);
    assertTrue(v.getMinor() == 0);
    assertTrue(v.getPatch() == 0);
    assertFalse(v.isSnapshot());
    assertTrue(v.getType() == VersionType.RELEASE);
    assertEquals(v.getParsedVersion(), "1.0.0");

    // test with snapshot
    v = Version.parse("1.1-SNAPSHOT");
    assertTrue(v.getMajor() == 1);
    assertTrue(v.getMinor() == 1);
    assertTrue(v.getPatch() == 0);
    assertTrue(v.isSnapshot());
    assertTrue(v.getType() == VersionType.RELEASE);
    assertEquals(v.getParsedVersion(), "1.1.0-SNAPSHOT");

    // test with maintenance
    v = Version.parse("2.3.14");
    assertTrue(v.getMajor() == 2);
    assertTrue(v.getMinor() == 3);
    assertTrue(v.getPatch() == 14);
    assertFalse(v.isSnapshot());
    assertTrue(v.getType() == VersionType.RELEASE);
    assertEquals(v.getParsedVersion(), "2.3.14");
  }

  /**
   * Method description
   *
   */
  @Test
  public void parseTypeVersions()
  {
    Version v = Version.parse("1.0-alpha");

    assertTrue(v.getMajor() == 1);
    assertTrue(v.getMinor() == 0);
    assertTrue(v.getPatch() == 0);
    assertFalse(v.isSnapshot());
    assertTrue(v.getType() == VersionType.ALPHA);
    assertTrue(v.getTypeVersion() == 1);
    assertEquals(v.getParsedVersion(), "1.0.0-alpha1");

    // Test release candidate
    v = Version.parse("2.1.2-RC3");
    assertTrue(v.getMajor() == 2);
    assertTrue(v.getMinor() == 1);
    assertTrue(v.getPatch() == 2);
    assertFalse(v.isSnapshot());
    assertTrue(v.getType() == VersionType.RELEASE_CANDIDAT);
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
    Version[] versions = new Version[9];

    versions[0] = Version.parse("2.3.1-SNAPSHOT");
    versions[1] = Version.parse("2.3.1-beta1");
    versions[2] = Version.parse("2.3.1-beta2");
    versions[3] = Version.parse("2.3.1-M1");
    versions[4] = Version.parse("2.3.1-alpha2");
    versions[5] = Version.parse("2.3.1-RC1");
    versions[6] = Version.parse("2.3.1");
    versions[7] = Version.parse("2.3");
    versions[8] = Version.parse("2.4.6");
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
    assertFalse(Version.parse("1.0").isNewer("1.0.1"));
    assertTrue(Version.parse("1.1").isNewer("1.1-alpha1"));
    assertTrue(Version.parse("1.1").isNewer("1.1-RC5"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testIsOlder()
  {
    assertFalse(Version.parse("1.0.1").isOlder("1.0"));
    assertTrue(Version.parse("1.1-alpha1").isOlder("1.1"));
    assertTrue(Version.parse("1.1-RC5").isOlder("1.1"));
  }

  @Test
  public void testIsOlderOrEqual() {
    assertTrue(Version.parse("1.0.0").isOlderOrEqual("1.0.1"));
    assertTrue(Version.parse("1.0.1").isOlderOrEqual("1.0.1"));
  }

  @Test
  public void testINewerOrEqual() {
    assertTrue(Version.parse("1.0.1").isNewerOrEqual("1.0.0"));
    assertTrue(Version.parse("1.0.1").isOlderOrEqual("1.0.1"));
  }

  /**
   * Method description
   *
   */
  @Test(expected = VersionParseException.class)
  public void testUnparseable()
  {
    Version.parse("aaaa");
  }
}
