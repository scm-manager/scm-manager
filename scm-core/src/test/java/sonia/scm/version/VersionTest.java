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


import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class VersionTest {

  @Test
  void parseSimpleVersion() {
    Version v = Version.parse("1.0");

    assertThat(v.getMajor()).isOne();
    assertThat(v.getMinor()).isZero();
    assertThat(v.getPatch()).isZero();
    assertThat(v.isSnapshot()).isFalse();
    assertThat(v.getType()).isSameAs(VersionType.RELEASE);
    assertThat(v.getParsedVersion()).isEqualTo("1.0.0");

    // test with snapshot
    v = Version.parse("1.1-SNAPSHOT");
    assertThat(v.getMajor()).isOne();
    assertThat(v.getMinor()).isOne();
    assertThat(v.getPatch()).isZero();
    assertThat(v.isSnapshot()).isTrue();
    assertThat(v.getType()).isSameAs(VersionType.RELEASE);
    assertThat(v.getParsedVersion()).isEqualTo("1.1.0-SNAPSHOT");

    // test with maintenance
    v = Version.parse("2.3.14");
    assertThat(v.getMajor()).isEqualTo(2);
    assertThat(v.getMinor()).isEqualTo(3);
    assertThat(v.getPatch()).isEqualTo(14);
    assertThat(v.isSnapshot()).isFalse();
    assertThat(v.getType()).isSameAs(VersionType.RELEASE);
    assertThat(v.getParsedVersion()).isEqualTo("2.3.14");
  }

  @Test
  void parseTypeVersions() {
    Version v = Version.parse("1.0-alpha");

    assertThat(v.getMajor()).isOne();
    assertThat(v.getMinor()).isZero();
    assertThat(v.getPatch()).isZero();
    assertThat(v.isSnapshot()).isFalse();
    assertThat(v.getType()).isSameAs(VersionType.ALPHA);
    assertThat(v.getTypeVersion()).isOne();
    assertThat(v.getParsedVersion()).isEqualTo("1.0.0-alpha1");

    // Test release candidate
    v = Version.parse("2.1.2-RC3");
    assertThat(v.getMajor()).isEqualTo(2);
    assertThat(v.getMinor()).isEqualTo(1);
    assertThat(v.getPatch()).isEqualTo(2);
    assertThat(v.isSnapshot()).isFalse();
    assertThat(v.getType()).isSameAs(VersionType.RELEASE_CANDIDAT);
    assertThat(v.getTypeVersion()).isEqualTo(3);
    assertThat(v.getParsedVersion()).isEqualTo("2.1.2-RC3");
  }

  @Test
  void testCompareTo() {
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
    assertThat(versions[0].getParsedVersion()).isEqualTo("2.4.6");
    assertThat(versions[1].getParsedVersion()).isEqualTo("2.3.1");
    assertThat(versions[2].getParsedVersion()).isEqualTo("2.3.1-SNAPSHOT");
    assertThat(versions[3].getParsedVersion()).isEqualTo("2.3.1-RC1");
    assertThat(versions[4].getParsedVersion()).isEqualTo("2.3.1-beta2");
    assertThat(versions[5].getParsedVersion()).isEqualTo("2.3.1-beta1");
    assertThat(versions[6].getParsedVersion()).isEqualTo("2.3.1-alpha2");
    assertThat(versions[7].getParsedVersion()).isEqualTo("2.3.1-M1");
    assertThat(versions[8].getParsedVersion()).isEqualTo("2.3.0");
  }

  @Test
  void testIsNewer() {
    assertThat(Version.parse("1.0").isNewer("1.0.1")).isFalse();
    assertThat(Version.parse("1.1").isNewer("1.1-alpha1")).isTrue();
    assertThat(Version.parse("1.1").isNewer("1.1-RC5")).isTrue();
  }

  @Test
  void testIsOlder() {
    assertThat(Version.parse("1.0.1").isOlder("1.0")).isFalse();
    assertThat(Version.parse("1.1-alpha1").isOlder("1.1")).isTrue();
    assertThat(Version.parse("1.1-RC5").isOlder("1.1")).isTrue();
  }

  @Test
  void testIsOlderOrEqual() {
    assertThat(Version.parse("1.0.0").isOlderOrEqual("1.0.1")).isTrue();
    assertThat(Version.parse("1.0.1").isOlderOrEqual("1.0.1")).isTrue();
  }

  @Test
  void testINewerOrEqual() {
    assertThat(Version.parse("1.0.1").isNewerOrEqual("1.0.0")).isTrue();
    assertThat(Version.parse("1.0.1").isOlderOrEqual("1.0.1")).isTrue();
  }

  @Test
  void testUnparseable() {
    assertThrows(VersionParseException.class, () -> Version.parse("aaaa"));
  }

  @Test
  void shouldDetectUniqueMavenSnapshotVersion() {
    Version version = Version.parse("1.0.0-20201022.094711-15");
    assertThat(version.isSnapshot()).isTrue();
    assertThat(version).hasToString("1.0.0-SNAPSHOT");
  }
}
