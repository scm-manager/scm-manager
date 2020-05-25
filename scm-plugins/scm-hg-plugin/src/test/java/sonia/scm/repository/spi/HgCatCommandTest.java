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

package sonia.scm.repository.spi;

import org.junit.Ignore;
import org.junit.Test;
import sonia.scm.NotFoundException;
import sonia.scm.repository.InternalRepositoryException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class HgCatCommandTest extends AbstractHgCommandTestBase {

  @Test
  public void testCat() throws IOException {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("a.txt");
    request.setRevision("a9bacaf1b7fa");
    assertEquals("a", execute(request));
  }

  @Test
  public void testSimpleCat() throws IOException {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("b.txt");
    assertEquals("b", execute(request));
  }

  @Test(expected = InternalRepositoryException.class)
  public void testUnknownFile() throws IOException {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("unknown");
    execute(request);
  }

  @Test(expected = NotFoundException.class)
  @Ignore("detection of unknown revision in hg not yet implemented")
  public void testUnknownRevision() throws IOException {
    CatCommandRequest request = new CatCommandRequest();

    request.setRevision("abc");
    request.setPath("a.txt");
    execute(request);
  }

  @Test
  public void testSimpleStream() throws IOException {
    CatCommandRequest request = new CatCommandRequest();
    request.setPath("b.txt");

    InputStream catResultStream = new HgCatCommand(cmdContext).getCatResultStream(request);

    assertEquals('b', catResultStream.read());
    assertEquals('\n', catResultStream.read());
    assertEquals(-1, catResultStream.read());

    catResultStream.close();
  }

  private String execute(CatCommandRequest request) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new HgCatCommand(cmdContext).getCatResult(request, baos);
    return baos.toString().trim();
  }
}
