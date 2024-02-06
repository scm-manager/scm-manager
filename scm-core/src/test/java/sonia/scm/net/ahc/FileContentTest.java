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
    
package sonia.scm.net.ahc;


import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


@RunWith(MockitoJUnitRunner.class)
public class FileContentTest
{


  @Test
  public void testPrepareRequest() throws IOException
  {
    FileContent content = create("abc");

    content.prepare(request);
    verify(request).contentLength(3l);
  }


  @Test
  public void testProcess() throws IOException
  {
    FileContent content = create("abc");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    content.process(baos);
    assertEquals("abc", baos.toString("UTF-8"));
  }

  private FileContent create(String value) throws IOException
  {
    File file = temp.newFile();

    Files.write("abc", file, Charsets.UTF_8);

    return new FileContent(file);
  }

  //~--- fields ---------------------------------------------------------------

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Mock
  private AdvancedHttpRequestWithBody request;
}
