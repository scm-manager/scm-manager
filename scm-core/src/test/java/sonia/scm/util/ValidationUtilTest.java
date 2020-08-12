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

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Sebastian Sdorra
 */
public class ValidationUtilTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testIsFilenameValid()
  {

    // true
    assertTrue(ValidationUtil.isFilenameValid("test"));
    assertTrue(ValidationUtil.isFilenameValid("test 123"));

    // false
    assertFalse(ValidationUtil.isFilenameValid("../../"));
    assertFalse(ValidationUtil.isFilenameValid("test/../.."));
    assertFalse(ValidationUtil.isFilenameValid("\\ka"));
    assertFalse(ValidationUtil.isFilenameValid("ka:on"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testIsMailAddressValid()
  {

    // true
    assertTrue(ValidationUtil.isMailAddressValid("s.sdorra@ostfalia.de"));
    assertTrue(ValidationUtil.isMailAddressValid("sdorra@ostfalia.de"));
    assertTrue(ValidationUtil.isMailAddressValid("s.sdorra@hbk-bs.de"));
    assertTrue(ValidationUtil.isMailAddressValid("s.sdorra@gmail.com"));
    assertTrue(ValidationUtil.isMailAddressValid("s.sdorra@t.co"));
    assertTrue(ValidationUtil.isMailAddressValid("s.sdorra@ucla.college")); 
    assertTrue(ValidationUtil.isMailAddressValid("s.sdorra@example.xn--p1ai"));
    
    // issue 909
    assertTrue(ValidationUtil.isMailAddressValid("s.sdorra@scm.solutions")); 

    // false
    assertFalse(ValidationUtil.isMailAddressValid("ostfalia.de"));
    assertFalse(ValidationUtil.isMailAddressValid("@ostfalia.de"));
    assertFalse(ValidationUtil.isMailAddressValid("s.sdorra@"));
    assertFalse(ValidationUtil.isMailAddressValid("s.sdorra@ostfalia"));
    assertFalse(ValidationUtil.isMailAddressValid("s.sdorra@@ostfalia.de"));
    assertFalse(ValidationUtil.isMailAddressValid("s.sdorra@ ostfalia.de"));
    assertFalse(ValidationUtil.isMailAddressValid("s.sdorra @ostfalia.de"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testIsNameValid()
  {

    // true
    assertTrue(ValidationUtil.isNameValid("test"));
    assertTrue(ValidationUtil.isNameValid("test.git"));
    assertTrue(ValidationUtil.isNameValid("Test123.git"));
    assertTrue(ValidationUtil.isNameValid("Test123-git"));
    assertTrue(ValidationUtil.isNameValid("Test_user-123.git"));
    assertTrue(ValidationUtil.isNameValid("test@scm-manager.de"));
    assertTrue(ValidationUtil.isNameValid("t"));

    // false
    assertFalse(ValidationUtil.isNameValid("test 123"));
    assertFalse(ValidationUtil.isNameValid(" test 123"));
    assertFalse(ValidationUtil.isNameValid(" test 123 "));
    assertFalse(ValidationUtil.isNameValid("test 123 "));
    assertFalse(ValidationUtil.isNameValid("test/123"));
    assertFalse(ValidationUtil.isNameValid("test%123"));
    assertFalse(ValidationUtil.isNameValid("test:123"));
    assertFalse(ValidationUtil.isNameValid("t "));
    assertFalse(ValidationUtil.isNameValid(" t"));
    assertFalse(ValidationUtil.isNameValid(" t "));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testIsNotContaining()
  {

    // true
    assertTrue(ValidationUtil.isNotContaining("test", "abc"));

    // false
    assertFalse(ValidationUtil.isNotContaining("test", "e"));
    assertFalse(ValidationUtil.isNotContaining("test", "e", "s"));
    assertFalse(ValidationUtil.isNotContaining("test", "es"));
    assertFalse(ValidationUtil.isNotContaining("test", "t"));
  }

  @Test
  public void testIsRepositoryNameValid() {
    String[] validPaths = {
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
    };

    // issue 142, 144 and 148
    String[] invalidPaths = {
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
    };

    for (String path : validPaths) {
      assertTrue(ValidationUtil.isRepositoryNameValid(path));
    }

    for (String path : invalidPaths) {
      assertFalse(ValidationUtil.isRepositoryNameValid(path));
    }
  }
}
