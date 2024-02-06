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
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GitCatCommand}.
 *
 * TODO add not found test
 *
 */
public class GitCatCommandTest extends AbstractGitCommandTestBase {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testDefaultBranch() throws IOException {
    // without default branch, the repository head should be used
    CatCommandRequest request = new CatCommandRequest();
    request.setPath("a.txt");

    assertEquals("a\nline for blame", execute(request));

    // set default branch for repository and check again
    createContext().setConfig(new GitRepositoryConfig("test-branch"));
    assertEquals("a and b", execute(request));
  }

  @Test
  public void testCat() throws IOException {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("a.txt");
    request.setRevision("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    assertEquals("a and b", execute(request));
  }

  @Test
  public void testSimpleCat() throws IOException {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("b.txt");
    assertEquals("b", execute(request));
  }

  @Test
  public void testUnknownFile() throws IOException {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("unknown");

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
  public void testUnknownRevision() throws IOException {
    CatCommandRequest request = new CatCommandRequest();

    request.setRevision("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    request.setPath("a.txt");

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
    request.setPath("b.txt");

    InputStream catResultStream = new GitCatCommand(createContext(), null).getCatResultStream(request);

    assertEquals('b', catResultStream.read());
    assertEquals('\n', catResultStream.read());
    assertEquals(-1, catResultStream.read());

    catResultStream.close();
  }

  @Test
  public void testLfsStream() throws IOException {
    LfsBlobStoreFactory lfsBlobStoreFactory = mock(LfsBlobStoreFactory.class);
    BlobStore blobStore = mock(BlobStore.class);
    Blob blob = mock(Blob.class);
    when(lfsBlobStoreFactory.getLfsBlobStore(repository)).thenReturn(blobStore);
    when(blobStore.get("d2252bd9fde1bb2ae7531b432c48262c3cbe4df4376008986980de40a7c9cf8b"))
      .thenReturn(blob);
    when(blob.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{'i', 's'}));

    CatCommandRequest request = new CatCommandRequest();
    request.setRevision("lfs-test");
    request.setPath("lfs-image.png");

    InputStream catResultStream = new GitCatCommand(createContext(), lfsBlobStoreFactory)
      .getCatResultStream(request);

    assertEquals('i', catResultStream.read());
    assertEquals('s', catResultStream.read());

    assertEquals(-1, catResultStream.read());

    catResultStream.close();
  }

  private String execute(CatCommandRequest request) throws IOException {
    String content = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try
    {
      new GitCatCommand(createContext(), null).getCatResult(request, baos);
    }
    finally
    {
      content = baos.toString().trim();
    }

    return content;
  }
}
