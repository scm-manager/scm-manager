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

package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.junit.Assert.*;


//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.net.URL;

/**
 *
 * @author Sebastian Sdorra
 */
public class PathWebResourceLoaderTest extends WebResourceLoaderTestBase {

  @Test
  public void shouldReturnNullForDirectories() throws IOException {
    File directory = temp.newFolder();
    assertTrue(new File(directory, "awesome").mkdir());

    WebResourceLoader resourceLoader = new PathWebResourceLoader(directory.toPath());
    assertNull(resourceLoader.getResource("awesome"));
  }


  @Test
  public void shouldReturnResource() throws IOException {
    File directory = temp.newFolder();
    URL url = file(directory, "myresource").toURI().toURL();

    WebResourceLoader resourceLoader =
      new PathWebResourceLoader(directory.toPath());

    assertEquals(url, resourceLoader.getResource("/myresource"));
    assertEquals(url, resourceLoader.getResource("myresource"));
    assertNull(resourceLoader.getResource("other"));
  }

  @Test
  public void shouldNotReturnPathsWithAbsolutePath() throws IOException {
    File base = temp.newFolder();

    File one = new File(base, "one");
    assertTrue(one.mkdirs());
    File two = new File(base, "two");
    assertTrue(two.mkdirs());

    File secret = new File(two, "secret");
    assertTrue(secret.createNewFile());

    WebResourceLoader resourceLoader = new PathWebResourceLoader(one.toPath());
    assertNull(resourceLoader.getResource(secret.getAbsolutePath()));
    assertNull(resourceLoader.getResource("/" + secret.getAbsolutePath()));
  }

  @Test
  public void shouldNotReturnPathsWithPathTraversal() throws IOException {
    File base = temp.newFolder();

    File one = new File(base, "one");
    assertTrue(one.mkdirs());
    File two = new File(base, "two");
    assertTrue(two.mkdirs());

    File secret = new File(two, "secret");
    assertTrue(secret.createNewFile());

    WebResourceLoader resourceLoader = new PathWebResourceLoader(one.toPath());
    assertNull(resourceLoader.getResource("../two/secret"));
  }
}
