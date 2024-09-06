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

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;


public class IOUtilTest
{


  @Test
  public void testIsChild() throws IOException
  {
    assertTrue(isChild("/tmp/test", "/tmp/test/a"));
    assertTrue(isChild("/tmp/test", "/tmp/test/a/b"));
    assertTrue(isChild("/tmp/test", "/tmp/test/a/b/c"));
    assertFalse(isChild("/tmp/test", "/tmp/test"));
    assertFalse(isChild("/tmp/test", "/tmp"));
    assertFalse(isChild("/tmp/test", "/"));
    assertFalse(isChild("/tmp/test", "/asd"));
    assertFalse(isChild("/tmp/test", "/asd/a/b"));
  }


  private File createMockFile(String canonicalPath) throws IOException
  {
    File file = mock(File.class);

    when(file.getCanonicalPath()).thenReturn(canonicalPath);

    return file;
  }


  /**
   * Method description
   *
   *
   * @param parentPath
   * @param path
   *
   * @return
   *
   * @throws IOException
   */
  private boolean isChild(String parentPath, String path) throws IOException
  {
    return IOUtil.isChild(createMockFile(parentPath), createMockFile(path));
  }
}
