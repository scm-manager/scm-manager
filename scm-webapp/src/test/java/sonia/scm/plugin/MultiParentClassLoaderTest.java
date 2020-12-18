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

import com.google.common.io.Resources;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FixedValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Sebastian Sdorra
 */
class MultiParentClassLoaderTest {

  @Test
  void shouldLoadClass(@TempDir Path directory) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
    byte[] classOneRaw = createDynamicClass("One", "Value of one");
    ClassLoader one = createClassLoader(directory, "One.class", classOneRaw);
    byte[] classTwoRaw = createDynamicClass("Two", "Value of two");
    ClassLoader two = createClassLoader(directory, "Two.class", classTwoRaw);

    MultiParentClassLoader classLoader = new MultiParentClassLoader(one, two);
    assertClassToString(classLoader, "One", "Value of one");
    assertClassToString(classLoader, "Two", "Value of two");
  }

  private void assertClassToString(ClassLoader classLoader, String className, String toStringValue) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    Class<?> aClass = classLoader.loadClass(className);
    assertThat(aClass.newInstance()).hasToString(toStringValue);
  }

  private byte[] createDynamicClass(String name, String toStringValue) {
    return new ByteBuddy()
      .subclass(Object.class)
      .name(name)
      .method(named("toString"))
      .intercept(FixedValue.value(toStringValue))
      .make()
      .getBytes();
  }

  @Test
  void shouldThrowClassNotFoundException(@TempDir Path directory) throws MalformedURLException {
    URLClassLoader one = createClassLoader(directory.resolve("one"));
    URLClassLoader two = createClassLoader(directory.resolve("two"));
    MultiParentClassLoader classLoader = new MultiParentClassLoader(one, two);
    assertThrows(ClassNotFoundException.class, () -> classLoader.loadClass("UnknownClass"));
  }

  @Test
  void shouldReturnResource(@TempDir Path directory) throws IOException {
    URLClassLoader one = createClassLoader(directory.resolve("one"), "one", "one");
    URLClassLoader two = createClassLoader(directory.resolve("two"), "two", "two");
    MultiParentClassLoader classLoader = new MultiParentClassLoader(one, two);

    assertResource(classLoader, "one", "one");
    assertResource(classLoader, "two", "two");
  }

  @Test
  void shouldReturnResources(@TempDir Path directory) throws IOException {
    URLClassLoader one = createClassLoader(directory.resolve("one"), "both", "one");
    URLClassLoader two = createClassLoader(directory.resolve("two"), "both", "two");
    MultiParentClassLoader classLoader = new MultiParentClassLoader(one, two);

    Enumeration<URL> both = classLoader.getResources("both");
    List<String> content = toStrings(both);

    assertThat(content).containsOnly("one", "two");
  }

  @SuppressWarnings("UnstableApiUsage")
  private List<String> toStrings(Enumeration<URL> urlEnumeration) throws IOException {
    List<String> content = new ArrayList<>();
    while (urlEnumeration.hasMoreElements()) {
      URL url = urlEnumeration.nextElement();
      content.add(Resources.toString(url, StandardCharsets.UTF_8));
    }
    return content;
  }

  @SuppressWarnings("UnstableApiUsage")
  private void assertResource(ClassLoader classLoader, String resource, String content) throws IOException {
    URL url = classLoader.getResource(resource);
    assertThat(url).isNotNull();
    String urlContent = Resources.toString(url, StandardCharsets.UTF_8);
    assertThat(urlContent).isEqualTo(content);
  }

  private URLClassLoader createClassLoader(Path directory, String resource, String content) throws IOException {
    return createClassLoader(directory, resource, content.getBytes(StandardCharsets.UTF_8));
  }

  private URLClassLoader createClassLoader(Path directory, String resource, byte[] content) throws IOException {
    Path file = directory.resolve(resource);
    Files.createDirectories(file.getParent());
    Files.write(file, content);
    return createClassLoader(directory);
  }

  private URLClassLoader createClassLoader(Path directory) throws MalformedURLException {
    ClassLoader parent = ClassLoader.getSystemClassLoader().getParent();
    return new URLClassLoader(new URL[]{directory.toUri().toURL()}, parent);
  }

}
