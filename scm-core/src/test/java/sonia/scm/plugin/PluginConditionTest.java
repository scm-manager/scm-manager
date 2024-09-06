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


import org.junit.Test;
import sonia.scm.plugin.PluginCondition.CheckResult;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;


public class PluginConditionTest {

  @Test
  public void testEmptyShouldBeSupported() {
    assertThat(new PluginCondition().getConditionCheckResult()).isEqualTo(CheckResult.OK);
  }

  @Test
  public void testArchIsSupported() {
    assertThat(new PluginCondition(null, null, "32")
      .getConditionCheckResult(null, null, "32"))
      .isEqualTo(CheckResult.OK);
    assertThat(new PluginCondition(null, null, "32")
      .getConditionCheckResult(null, null, "64"))
      .isEqualTo(CheckResult.ARCHITECTURE_MISMATCH);
  }

  @Test
  public void testIsOsSupported() {
    assertThat(new PluginCondition(null, List.of("linux"), null)
      .getConditionCheckResult(null, "linux", null))
      .isEqualTo(CheckResult.OK);
    assertThat(new PluginCondition(null, List.of("unix"), null)
      .getConditionCheckResult(null, "Mac OS X", null))
      .isEqualTo(CheckResult.OK);
    assertThat(new PluginCondition(null, List.of("unix"), null)
      .getConditionCheckResult(null, "Solaris", null))
      .isEqualTo(CheckResult.OK);
    assertThat(new PluginCondition(null, List.of("posix"), null)
      .getConditionCheckResult(null, "Linux", null))
      .isEqualTo(CheckResult.OK);
    assertThat(new PluginCondition(null, List.of("win"), null)
      .getConditionCheckResult(null, "Windows 2000", null))
      .isEqualTo(CheckResult.OK);
    assertThat(new PluginCondition(null, List.of("win"), null)
      .getConditionCheckResult(null, "Mac OS X", null))
      .isEqualTo(CheckResult.OS_MISMATCH);
  }

  @Test
  public void testIsSupported() {
    assertTrue(new PluginCondition("1.2", List.of("Mac"), "64")
      .isSupported("1.4", "Mac OS X", "64"));
  }

  @Test
  public void testVersionIsSupported() {
    assertThat(new PluginCondition("1.1", null, null)
      .getConditionCheckResult("1.2", null, null))
      .isEqualTo(CheckResult.OK);
    assertThat(new PluginCondition("1.0", null, null)
      .getConditionCheckResult("1.1-SNAPSHOT", null, null))
      .isEqualTo(CheckResult.OK);
    assertThat(new PluginCondition("1.1", null, null)
      .getConditionCheckResult("1.1", null, null))
      .isEqualTo(CheckResult.OK);
  }

  @Test
  public void testConditionCheckResultIsOk() {
    assertThat(new PluginCondition("1.2", List.of("Mac"), "64")
      .getConditionCheckResult("1.4", "Mac OS X", "64")).isEqualTo(CheckResult.OK);
  }

  @Test
  public void testConditionCheckResultIsVersionMismatch() {
    assertThat(new PluginCondition("1.4", List.of("Mac"), "64")
      .getConditionCheckResult("1.2", "Mac OS X", "64"))
      .isEqualTo(CheckResult.VERSION_MISMATCH);
  }

  @Test
  public void testConditionCheckResultIsOSMismatch() {
    assertThat(new PluginCondition("1.2", List.of("Win"), "64")
      .getConditionCheckResult("1.4", "Mac OS X", "64"))
      .isEqualTo(CheckResult.OS_MISMATCH);
  }

  @Test
  public void testConditionCheckResultIsArchMismatch() {
    assertThat(new PluginCondition("1.2", List.of("Mac"), "64")
      .getConditionCheckResult("1.4", "Mac OS X", "32"))
      .isEqualTo(CheckResult.ARCHITECTURE_MISMATCH);
  }

  @Test
  public void testToStringWithAllConditions() {
    assertThat(new PluginCondition("1.2", asList("Mac", "Win"), "64").toString())
      .isEqualTo("Required conditions: minimal SCM version: 1.2; supported OS: Mac Win; architecture: 64");
  }

  @Test
  public void testToStringWithZeroConditions() {
    assertThat(new PluginCondition().toString()).isEqualTo("No required conditions.");
  }

  @Test
  public void testToStringWithVersionCondition() {
    assertThat(new PluginCondition("1.2", null, null).toString())
      .isEqualTo("Required conditions: minimal SCM version: 1.2");
  }

  @Test
  public void testToStringWithOneOSCondition() {
    assertThat(new PluginCondition(null, List.of("Mac"), null).toString())
      .isEqualTo("Required conditions: supported OS: Mac");
  }

  @Test
  public void testToStringWithMultipleOSCondition() {
    assertThat(new PluginCondition(null, asList("Mac", "Win"), null).toString())
      .isEqualTo("Required conditions: supported OS: Mac Win");
  }

  @Test
  public void testToStringWithArchCondition() {
    assertThat(new PluginCondition(null, null, "64").toString())
      .isEqualTo("Required conditions: architecture: 64");
  }

  @Test
  public void testToStringWithVersionAndOSCondition() {
    assertThat(new PluginCondition("1.2", List.of("Mac"), null).toString())
      .isEqualTo("Required conditions: minimal SCM version: 1.2; supported OS: Mac");
  }

  @Test
  public void testToStringWithVersionAndArchCondition() {
    assertThat(new PluginCondition("1.2", null, "64").toString())
      .isEqualTo("Required conditions: minimal SCM version: 1.2; architecture: 64");
  }

  @Test
  public void testToStringWithOSAndArchCondition() {
    assertThat(new PluginCondition(null, List.of("Mac"), "64").toString())
      .isEqualTo("Required conditions: supported OS: Mac; architecture: 64");
  }
}
