package sonia.scm.plugin;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

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

@ExtendWith(TempDirectory.class)
class UberClassLoaderTest {

  private final URLClassLoader parentClassLoader = new URLClassLoader(new URL[0]);

  @Test
  void shouldOnlyUseClassloaderOnce(@TempDirectory.TempDir Path tempDir) throws IOException {
    ClassLoader mailClassLoader = createClassLoader(tempDir, "plugin.txt", "mail");
    ClassLoader reviewClassLoader = createClassLoader(mailClassLoader, tempDir, "plugin.txt", "review");

    UberClassLoader uberClassLoader = new UberClassLoader(parentClassLoader, ImmutableSet.of(mailClassLoader, reviewClassLoader));
    List<URL> resources = Collections.list(uberClassLoader.findResources("plugin.txt"));

    assertThat(resources).hasSize(2);
    assertThat(toContent(resources)).containsOnly("mail", "review");
  }

  @Test
  void shouldReturnResourceFromEachPluginClassLoader(@TempDirectory.TempDir Path tempDir) throws IOException {
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
