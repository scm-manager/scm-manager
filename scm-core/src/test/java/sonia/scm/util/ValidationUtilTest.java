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
    assertTrue(ValidationUtil.isNameValid("Test_repository-123.git"));

    // false
    assertFalse(ValidationUtil.isNameValid("test 123"));
    assertFalse(ValidationUtil.isNameValid("test@123"));
    assertFalse(ValidationUtil.isNameValid("test/123"));
    assertFalse(ValidationUtil.isNameValid("test%123"));
    assertFalse(ValidationUtil.isNameValid("test:123"));
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

  /**
   * Method description
   *
   */
  @Test
  public void testIsUsernameValid()
  {

    // true
    assertTrue(ValidationUtil.isUsernameValid("test"));
    assertTrue(ValidationUtil.isUsernameValid("test.git"));
    assertTrue(ValidationUtil.isUsernameValid("Test123.git"));
    assertTrue(ValidationUtil.isUsernameValid("Test123-git"));
    assertTrue(ValidationUtil.isUsernameValid("Test_user-123.git"));
    assertTrue(ValidationUtil.isUsernameValid("test@scm-manager.de"));
    assertTrue(ValidationUtil.isUsernameValid("test 123"));

    // false
    assertFalse(ValidationUtil.isUsernameValid(" test 123"));
    assertFalse(ValidationUtil.isUsernameValid(" test 123 "));
    assertFalse(ValidationUtil.isUsernameValid("test 123 "));
    assertFalse(ValidationUtil.isUsernameValid("test/123"));
    assertFalse(ValidationUtil.isUsernameValid("test%123"));
    assertFalse(ValidationUtil.isUsernameValid("test:123"));
  }
}
