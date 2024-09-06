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

package sonia.scm.plugin;


import org.junit.Test;

import static org.junit.Assert.*;


import java.io.File;
import java.io.IOException;

import java.net.URL;


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
