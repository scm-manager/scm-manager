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

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UberClassLoaderTest {

  private final URLClassLoader parentClassLoader = new URLClassLoader(new URL[0]);

  @Test
  void shouldOnlyUseClassloaderOnce(@TempDir Path tempDir) throws IOException {
    ClassLoader mailClassLoader = createClassLoader(tempDir, "plugin.txt", "mail");
    ClassLoader reviewClassLoader = createClassLoader(mailClassLoader, tempDir, "plugin.txt", "review");

    UberClassLoader uberClassLoader = new UberClassLoader(parentClassLoader, ImmutableSet.of(mailClassLoader, reviewClassLoader));
    List<URL> resources = Collections.list(uberClassLoader.findResources("plugin.txt"));

    assertThat(resources).hasSize(2);
    assertThat(toContent(resources)).containsOnly("mail", "review");
  }

  @Test
  void shouldReturnResourceFromEachPluginClassLoader(@TempDir Path tempDir) throws IOException {
    ClassLoader mailClassLoader = createClassLoader(tempDir, "scm.txt", "mail");
    ClassLoader reviewClassLoader = createClassLoader(tempDir, "scm.txt", "review");

    UberClassLoader uberClassLoader = new UberClassLoader(parentClassLoader, ImmutableSet.of(mailClassLoader, reviewClassLoader));
    List<URL> resources = Collections.list(uberClassLoader.findResources("scm.txt"));
    assertThat(toContent(resources)).containsOnly("mail", "review");
  }

  @SuppressWarnings("UnstableApiUsage")
  private List<String> toContent(Iterable<URL> resources) throws IOException {
    List<String> content = new ArrayList<>();
    for (URL resource : resources) {
      content.add(Resources.toString(resource, StandardCharsets.UTF_8));
    }
    return content;
  }

  private ClassLoader createClassLoader(Path tempDir, String resource, String value) throws IOException {
    return createClassLoader(Thread.currentThread().getContextClassLoader(), tempDir, resource, value);
  }

  private ClassLoader createClassLoader(ClassLoader parent, Path tempDir, String resource, String value) throws IOException {
    Path directory = tempDir.resolve(UUID.randomUUID().toString());
    Files.createDirectory(directory);

    Files.write(directory.resolve(resource), value.getBytes(StandardCharsets.UTF_8));

    return new URLClassLoader(new URL[]{
      directory.toUri().toURL()
    }, parent);
  }


}
