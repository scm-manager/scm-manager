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

package sonia.scm.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ValidationUtilTest {

  @ParameterizedTest
  @ValueSource(strings = {
    "test",
    "test 123"
  })
  void shouldAcceptFilename(String value) {
    assertTrue(ValidationUtil.isFilenameValid(value));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "../../",
    "test/../..",
    "\\ka, \"ka:on\""
  })
  void shouldRejectFilename(String value) {
    assertFalse(ValidationUtil.isFilenameValid(value));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "test",
    "test 123"
  })
  void shouldAcceptPath(String value) {
    // true
    assertTrue(ValidationUtil.isPathValid(value));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "..",
    "../",
    "some\\windows",
    "../../",
    "../ka",
    "test/../.."
  })
  void shouldRejectPath(String value) {
    assertFalse(ValidationUtil.isPathValid(value));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "s.sdorra@ostfalia.de",
    "sdorra@ostfalia.de",
    "s.sdorra@hbk-bs.de",
    "s.sdorra@gmail.com",
    "s.sdorra@t.co",
    "s.sdorra@ucla.college",
    "s.sdorra@example.xn--p1ai",
    "s.sdorra@scm.solutions", // issue 909
    "s'sdorra@scm.solutions",
    "\"S Sdorra\"@scm.solutions",
    "A@BC.DE",
    "x@example.com",
    "example@s.example",
    "user.name+tag+sorting@example.com",
    "name/surname@example.com",
    "user%example.com@example.org"
  })
  void shouldAcceptMailAddress(String value) {
    assertTrue(ValidationUtil.isMailAddressValid(value));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "ostfalia.de",
    "@ostfalia.de",
    "s.sdorra@",
    "s.sdorra@ostfalia",
    "s.sdorra@@ostfalia.de",
    "s.sdorra@ ostfalia.de",
    "s.sdorra @ostfalia.de",
    "s.sdorra@[ostfalia.de",
    "abc.example.com",
    "a@b@c@example.com"
  })
  void shouldRejectMailAddress(String value) {
    assertFalse(ValidationUtil.isMailAddressValid(value));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "test",
    "test.git",
    "Test123.git",
    "Test123-git",
    "Test_user-123.git",
    "test@scm-manager.de",
    "t",
    "Лорем-ипсум",
    "Λορεμ.ιπσθμ",
    "լոռեմիպսում",
    "ლორემიფსუმ",
    "प्रमान",
    "詳性約",
    "隠サレニ",
    "법률",
    "المدن",
    "אחד",
    "Hu-rëm"
  })
  void shouldAcceptName(String value) {
    assertTrue(ValidationUtil.isNameValid(value));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "@",
    "@test",
    " test123",
    "test/123",
    "test:123",
    "test#123",
    "test%123",
    "test&123",
    "test?123",
    "test=123",
    "test;123",
    "@test123",
    "t ",
    " t",
    " t ",
    ".."
  })
  void shouldRejectName(String value) {
    assertFalse(ValidationUtil.isNameValid(value));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "1",
    " ",
    "asdf"
  })
  void shouldRejectPassword(String value) {
    assertFalse(ValidationUtil.isPasswordValid(value));
  }

  @Test
  void testIsNotContaining() {

    // true
    assertTrue(ValidationUtil.isNotContaining("test", "abc"));

    // false
    assertFalse(ValidationUtil.isNotContaining("test", "e"));
    assertFalse(ValidationUtil.isNotContaining("test", "e", "s"));
    assertFalse(ValidationUtil.isNotContaining("test", "es"));
    assertFalse(ValidationUtil.isNotContaining("test", "t"));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "scm",
    "scm-",
    "scm_",
    "s_cm",
    "s-cm",
    "s",
    "sc",
    ".hiddenrepo",
    "b.",
    "...",
    "..c",
    "d..",
    "a..c"
  })
  void shouldAcceptRepositoryName(String path) {
    assertTrue(ValidationUtil.isRepositoryNameValid(path));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    ".",
    "/",
    "//",
    "..",
    "/.",
    "/..",
    "./",
    "../",
    "/../",
    "/./",
    "/...",
    "/abc",
    ".../",
    "/sdf/",
    "asdf/",
    "./b",
    "scm/plugins/.",
    "scm/../plugins",
    "scm/main/",
    "/scm/main/",
    "scm/./main",
    "scm//main",
    "scm\\main",
    "scm/main-$HOME",
    "scm/main-${HOME}-home",
    "scm/main-%HOME-home",
    "scm/main-%HOME%-home",
    "abc$abc",
    "abc%abc",
    "abc<abc",
    "abc>abc",
    "abc#abc",
    "abc+abc",
    "abc{abc",
    "abc}abc",
    "abc(abc",
    "abc)abc",
    "abc[abc",
    "abc]abc",
    "abc|abc",
    "scm/main",
    "scm/plugins/git-plugin",
    ".scm/plugins",
    "a/b..",
    "a/..b",
    "scm/main",
    "scm/plugins/git-plugin",
    "_scm",
    "-scm",
    "scm.git",
    "scm.git.git"
  })
  void shouldRejectRepositoryName(String path) {
    assertFalse(ValidationUtil.isRepositoryNameValid(path));
  }
}
