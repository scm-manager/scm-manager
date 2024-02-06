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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import sonia.scm.NotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class SvnCatCommandTest extends AbstractSvnCommandTestBase {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testCat() {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("a.txt");
    request.setRevision("1");
    assertEquals("a", execute(request));
  }

  @Test
  public void testSimpleCat() {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("c/d.txt");
    assertEquals("d", execute(request));
  }

  @Test
  public void testUnknownFile() {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("unknown");
    request.setRevision("1");

    expectedException.expect(new BaseMatcher<Object>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("expected NotFoundException for path");
      }

      @Override
      public boolean matches(Object item) {
        return "Path".equals(((NotFoundException)item).getContext().get(0).getType());
      }
    });

    execute(request);
  }

  @Test
  public void testUnknownRevision() {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("a.txt");
    request.setRevision("42");

    expectedException.expect(new BaseMatcher<Object>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("expected NotFoundException for revision");
      }

      @Override
      public boolean matches(Object item) {
        return "Revision".equals(((NotFoundException)item).getContext().get(0).getType());
      }
    });

    execute(request);
  }

  @Test
  public void testSimpleStream() throws IOException {
    CatCommandRequest request = new CatCommandRequest();
    request.setPath("a.txt");
    request.setRevision("1");

    InputStream catResultStream = new SvnCatCommand(createContext()).getCatResultStream(request);

    assertEquals('a', catResultStream.read());
    assertEquals('\n', catResultStream.read());
    assertEquals(-1, catResultStream.read());

    catResultStream.close();
  }

  private String execute(CatCommandRequest request) {
    String content = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try
    {
      new SvnCatCommand(createContext()).getCatResult(request, baos);
    }
    finally
    {
      content = baos.toString().trim();
    }

    return content;
  }

  @Test
  public void shouldNotThrowExceptionForNonFileNodeCatCommand() throws IOException {
    CatCommandRequest request = new CatCommandRequest();
    request.setPath("/");
    request.setRevision("1");

    InputStream catResultStream = new SvnCatCommand(createContext()).getCatResultStream(request);

    catResultStream.close();
  }
}
