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

package sonia.scm.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Sebastian Sdorra
 */
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
    "s.sdorra@scm.solutions" // issue 909
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
    "s.sdorra @ostfalia.de"
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

  @Test
  void shouldAcceptEncrypted() {
    assertTrue(ValidationUtil.isPasswordValid("$shiro1$SHA-512$8196$$secret"));
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
