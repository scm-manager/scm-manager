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
