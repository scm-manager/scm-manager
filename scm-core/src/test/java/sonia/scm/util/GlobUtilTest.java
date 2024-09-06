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


import org.junit.Test;

import static org.junit.Assert.*;


public class GlobUtilTest
{

   @Test
  public void convertGlobToRegExTest()
  {
    assertEquals("/test/path/.*", GlobUtil.convertGlobToRegEx("/test/path/*"));
    assertEquals(".*/somefile\\.txt",
                 GlobUtil.convertGlobToRegEx("*/somefile.txt"));
    assertEquals("a.d", GlobUtil.convertGlobToRegEx("a?d"));
  }

   @Test
  public void matchesTest()
  {
    assertTrue(GlobUtil.matches("/test/path/*", "/test/path/somefile.txt"));
    assertTrue(GlobUtil.matches("*/somefile.txt", "/test/path/somefile.txt"));
    assertTrue(GlobUtil.matches("a*d", "asd"));
    assertTrue(GlobUtil.matches("a?d", "asd"));
    assertFalse(GlobUtil.matches("a\\*d", "asd"));
    assertFalse(GlobUtil.matches("a\\?d", "asd"));
  }
}
