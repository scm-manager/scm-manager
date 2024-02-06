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

import org.junit.Test;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.spi.SyncAsyncExecutors.AsyncExecutorStepper;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static java.util.OptionalLong.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.repository.spi.SyncAsyncExecutors.stepperAsynchronousExecutor;
import static sonia.scm.repository.spi.SyncAsyncExecutors.synchronousExecutor;

/**
 * Unit tests for {@link GitBrowseCommand}.
 *
 */
public class GitBrowseCommandTest extends AbstractGitCommandTestBase {

  private final LfsBlobStoreFactory lfsBlobStoreFactory = mock(LfsBlobStoreFactory.class);

  @Test
  public void testDefaultBranch() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setPath("a.txt");
    BrowserResult result = createCommand().getBrowserResult(request);
    FileObject fileObject = result.getFile();
    assertEquals("a.txt", fileObject.getName());
    assertFalse(fileObject.isTruncated());
  }

  @Test
  public void testDefaultDefaultBranch() throws IOException {
    // without default branch, the repository head should be used
    FileObject root = createCommand().getBrowserResult(new BrowseCommandRequest()).getFile();
    assertNotNull(root);

    Collection<FileObject> foList = root.getChildren();
    assertNotNull(foList);
    assertFalse(foList.isEmpty());

    assertThat(foList)
      .extracting("name")
      .containsExactly("c", "a.txt", "b.txt", "f.txt");
  }

  @Test
  public void testExplicitDefaultBranch() throws IOException {
    createContext().setConfig(new GitRepositoryConfig("test-branch"));

    FileObject root = createCommand().getBrowserResult(new BrowseCommandRequest()).getFile();
    assertNotNull(root);

    Collection<FileObject> foList = root.getChildren();
    assertThat(foList)
      .extracting("name")
      .containsExactly("c", "a.txt");
  }

  @Test
  public void testBrowse() throws IOException {
    FileObject root = createCommand().getBrowserResult(new BrowseCommandRequest()).getFile();
    assertNotNull(root);

    Collection<FileObject> foList = root.getChildren();

    FileObject a = findFile(foList, "a.txt");
    FileObject c = findFile(foList, "c");

    assertFalse(a.isDirectory());
    assertEquals("a.txt", a.getName());
    assertEquals("a.txt", a.getPath());
    assertEquals("added new line for blame", a.getDescription().get());
    assertTrue(a.getLength().getAsLong() > 0);
    checkDate(a.getCommitDate().getAsLong());

    assertTrue(c.isDirectory());
    assertEquals("c", c.getName());
    assertEquals("c", c.getPath());
  }

  @Test
  public void testAsynchronousBrowse() throws IOException {
    try (AsyncExecutorStepper executor = stepperAsynchronousExecutor()) {
      GitBrowseCommand command = new GitBrowseCommand(createContext(), null, executor);
      List<BrowserResult> updatedResults = new LinkedList<>();
      BrowseCommandRequest request = new BrowseCommandRequest(updatedResults::add);
      FileObject root = command.getBrowserResult(request).getFile();
      assertNotNull(root);

      Collection<FileObject> foList = root.getChildren();

      FileObject a = findFile(foList, "a.txt");
      FileObject b = findFile(foList, "b.txt");

      assertTrue(a.isPartialResult());
      assertFalse("expected empty name before commit could have been read", a.getDescription().isPresent());
      assertFalse("expected empty date before commit could have been read", a.getCommitDate().isPresent());
      assertTrue(b.isPartialResult());
      assertFalse("expected empty name before commit could have been read", b.getDescription().isPresent());
      assertFalse("expected empty date before commit could have been read", b.getCommitDate().isPresent());

      executor.next();

      assertEquals(1, updatedResults.size());
      assertFalse(a.isPartialResult());
      assertNotNull("expected correct name after commit could have been read", a.getDescription());
      assertTrue("expected correct date after commit could have been read", a.getCommitDate().isPresent());
      assertTrue(b.isPartialResult());
      assertFalse("expected empty name before commit could have been read", b.getDescription().isPresent());
      assertFalse("expected empty date before commit could have been read", b.getCommitDate().isPresent());

      executor.next();

      assertEquals(2, updatedResults.size());
      assertFalse(b.isPartialResult());
      assertNotNull("expected correct name after commit could have been read", b.getDescription());
      assertTrue("expected correct date after commit could have been read", b.getCommitDate().isPresent());
    }
  }

  @Test
  public void testBrowseSubDirectory() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setPath("c");

    FileObject root = createCommand().getBrowserResult(request).getFile();

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList)
      .extracting("name")
      .containsExactly("d.txt", "e.txt");

    FileObject d = findFile(foList, "d.txt");
    FileObject e = findFile(foList, "e.txt");

    assertFalse(d.isDirectory());
    assertEquals("d.txt", d.getName());
    assertEquals("c/d.txt", d.getPath());
    assertEquals("added file d and e in folder c", d.getDescription().get());
    assertTrue(d.getLength().getAsLong() > 0);
    checkDate(d.getCommitDate().getAsLong());

    assertFalse(e.isDirectory());
    assertEquals("e.txt", e.getName());
    assertEquals("c/e.txt", e.getPath());
    assertEquals("added file d and e in folder c", e.getDescription().get());
    assertTrue(e.getLength().getAsLong() > 0);
    checkDate(e.getCommitDate().getAsLong());
  }

  @Test
  public void testRecursive() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setRecursive(true);

    FileObject root = createCommand().getBrowserResult(request).getFile();

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList)
      .extracting("name")
      .containsExactly("c", "a.txt", "b.txt", "f.txt");

    FileObject c = findFile(foList, "c");

    Collection<FileObject> cChildren = c.getChildren();
    assertThat(cChildren)
      .extracting("name")
      .containsExactly("d.txt", "e.txt");
  }

  @Test
  public void testLfsSupport() throws IOException {
    BlobStore blobStore = mock(BlobStore.class);
    Blob blob = mock(Blob.class);
    when(lfsBlobStoreFactory.getLfsBlobStore(repository)).thenReturn(blobStore);
    when(blobStore.get("d2252bd9fde1bb2ae7531b432c48262c3cbe4df4376008986980de40a7c9cf8b")).thenReturn(blob);
    when(blob.getSize()).thenReturn(42L);

    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setRevision("lfs-test");
    FileObject root = createCommand().getBrowserResult(request).getFile();
    assertNotNull(root);

    Collection<FileObject> foList = root.getChildren();
    assertThat(foList)
      .filteredOn(f -> "lfs-image.png".equals(f.getName()))
      .extracting("length")
      .containsExactly(of(42L));
  }

  @Test
  public void testBrowseLimit() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setLimit(1);
    FileObject root = createCommand()
      .getBrowserResult(request).getFile();
    assertNotNull(root);

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList).extracting("name").containsExactly("c", "a.txt");
    assertThat(foList).hasSize(2);
    assertTrue("result should be marked as trunctated", root.isTruncated());
  }

  @Test
  public void testBrowseLimitWithoutTruncation() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setLimit(3);
    FileObject root = createCommand()
      .getBrowserResult(request).getFile();
    assertNotNull(root);

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList).extracting("name").containsExactly("c", "a.txt", "b.txt", "f.txt");
    assertThat(foList).hasSize(4);
    assertFalse("result should not be marked as trunctated", root.isTruncated());
  }

  @Test
  public void testBrowseOffset() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setLimit(1);
    request.setOffset(2);
    FileObject root = createCommand()
      .getBrowserResult(request).getFile();
    assertNotNull(root);

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList).extracting("name").containsExactly("f.txt");
    assertFalse("result should not be marked as trunctated", root.isTruncated());
  }

  @Test
  public void testRecursiveLimit() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setLimit(3);
    request.setRecursive(true);

    FileObject root = createCommand().getBrowserResult(request).getFile();

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList)
      .extracting("name")
      .containsExactly("c", "a.txt");

    FileObject c = findFile(foList, "c");

    Collection<FileObject> cChildren = c.getChildren();
    assertThat(cChildren)
      .extracting("name")
      .containsExactly("d.txt", "e.txt");
  }

  @Test
  public void testRecursiveLimitInSubDir() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setLimit(1);
    request.setRecursive(true);

    FileObject root = createCommand().getBrowserResult(request).getFile();

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList)
      .extracting("name")
      .containsExactly("c");

    FileObject c = findFile(foList, "c");

    Collection<FileObject> cChildren = c.getChildren();
    assertThat(cChildren)
      .extracting("name")
      .containsExactly("d.txt");
  }

  @Test
  public void testRecursiveOffset() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setOffset(1);
    request.setRecursive(true);

    FileObject root = createCommand().getBrowserResult(request).getFile();

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList)
      .extracting("name")
      .containsExactly("c", "a.txt", "b.txt", "f.txt");

    FileObject c = findFile(foList, "c");

    Collection<FileObject> cChildren = c.getChildren();
    assertThat(cChildren)
      .extracting("name")
      .containsExactly("e.txt");
  }

  @Test
  public void testRecursionWithDeepPaths() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setRevision("deep-folders");
    request.setRecursive(true);

    FileObject root = createCommand().getBrowserResult(request).getFile();

    assertThat(root.getChildren())
      .extracting("name")
      .containsExactly("c", "a.txt", "b.txt", "f.txt");
    FileObject c = findFile(root.getChildren(), "c");

    assertThat(c.getChildren())
      .extracting("name")
      .containsExactly("1", "4", "d.txt", "e.txt");

    FileObject f_1 = findFile(c.getChildren(), "1");
    assertThat(f_1.getChildren())
      .extracting("name")
      .containsExactly("2");
    FileObject f_12 = findFile(f_1.getChildren(), "2");
    assertThat(f_12.getChildren())
      .extracting("name")
      .containsExactly("3");
    FileObject f_123 = findFile(f_12.getChildren(), "3");
    assertThat(f_123.getChildren())
      .extracting("name")
      .containsExactly("123.txt");

    FileObject f_4 = findFile(c.getChildren(), "4");
    assertThat(f_4.getChildren())
      .extracting("name")
      .containsExactly("5", "6");
    FileObject f_45 = findFile(f_4.getChildren(), "5");
    assertThat(f_45.getChildren())
      .extracting("name")
      .containsExactly("45-1.txt", "45-2.txt");
    FileObject f_46 = findFile(f_4.getChildren(), "6");
    assertThat(f_46.getChildren())
      .extracting("name")
      .containsExactly("46-1.txt", "46-2.txt");
  }

  private FileObject findFile(Collection<FileObject> foList, String name) {
    return foList.stream()
      .filter(f -> name.equals(f.getName()))
      .findFirst()
      .orElseThrow(() -> new AssertionError("file " + name + " not found"));
  }

  private GitBrowseCommand createCommand() {
    return new GitBrowseCommand(createContext(), lfsBlobStoreFactory, synchronousExecutor());
  }
}
