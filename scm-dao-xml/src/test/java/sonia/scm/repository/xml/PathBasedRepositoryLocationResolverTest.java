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

package sonia.scm.repository.xml;

import com.google.common.base.Charsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.SCMContextProvider;
import sonia.scm.io.DefaultFileSystem;
import sonia.scm.io.FileSystem;
import sonia.scm.repository.InitialRepositoryLocationResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PathBasedRepositoryLocationResolverTest {

  private static final long CREATION_TIME = 42;

  @Mock
  private SCMContextProvider contextProvider;

  @Mock
  private InitialRepositoryLocationResolver initialRepositoryLocationResolver;

  @Mock
  private Clock clock;

  private final FileSystem fileSystem = new DefaultFileSystem();

  private Path basePath;

  private PathBasedRepositoryLocationResolver resolver;

  @BeforeEach
  void beforeEach(@TempDir Path temp) {
    this.basePath = temp;
    when(contextProvider.getBaseDirectory()).thenReturn(temp.toFile());
    when(contextProvider.resolve(any(Path.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(initialRepositoryLocationResolver.getPath(anyString())).thenAnswer(invocation -> temp.resolve(invocation.getArgument(0).toString()));
    when(clock.millis()).thenReturn(CREATION_TIME);
    resolver = createResolver();
  }

  @Test
  void shouldCreateInitialDirectory() {
    Path path = resolver.forClass(Path.class).createLocation("newId");

    assertThat(path).isEqualTo(basePath.resolve("newId"));
    assertThat(path).isDirectory();
  }

  @Test
  void shouldPersistInitialDirectory() {
    resolver.forClass(Path.class).createLocation("newId");

    String content = getXmlFileContent();

    assertThat(content).contains("newId");
    assertThat(content).contains(basePath.resolve("newId").toString());
  }

  @Test
  void shouldPersistWithCreationDate() {
    long now = CREATION_TIME + 100;
    when(clock.millis()).thenReturn(now);

    resolver.forClass(Path.class).createLocation("newId");

    assertThat(resolver.getCreationTime()).isEqualTo(CREATION_TIME);

    String content = getXmlFileContent();
    assertThat(content).contains("creation-time=\"" + CREATION_TIME + "\"");
  }

  @Test
  void shouldUpdateWithModifiedDate() {
    long now = CREATION_TIME + 100;
    when(clock.millis()).thenReturn(now);

    resolver.forClass(Path.class).createLocation("newId");

    assertThat(resolver.getCreationTime()).isEqualTo(CREATION_TIME);
    assertThat(resolver.getLastModified()).isEqualTo(now);

    String content = getXmlFileContent();
    assertThat(content).contains("creation-time=\"" + CREATION_TIME + "\"");
    assertThat(content).contains("last-modified=\"" + now + "\"");
  }

  @Nested
  class WithExistingData {

    private PathBasedRepositoryLocationResolver resolverWithExistingData;

    @BeforeEach
    void createExistingDatabase() {
      resolver.forClass(Path.class).createLocation("existingId_1");
      resolver.forClass(Path.class).createLocation("existingId_2");
      resolverWithExistingData = createResolver();
    }

    @Test
    void shouldInitWithExistingData() {
      Map<String, Path> foundRepositories = new HashMap<>();
      resolverWithExistingData.forClass(Path.class).forAllLocations(
        foundRepositories::put
      );
      assertThat(foundRepositories)
        .containsKeys("existingId_1", "existingId_2");
    }

    @Test
    void shouldRemoveFromFile() {
      resolverWithExistingData.remove("existingId_1");

      assertThat(getXmlFileContent()).doesNotContain("existingId_1");
    }

    @Test
    void shouldNotUpdateModificationDateForExistingDirectoryMapping() {
      long now = CREATION_TIME + 100;
      Path path = resolverWithExistingData.create(Path.class).getLocation("existingId_1");

      assertThat(path).isEqualTo(basePath.resolve("existingId_1"));

      String content = getXmlFileContent();
      assertThat(content).doesNotContain("last-modified=\"" + now + "\"");
    }

    @Test
    void shouldNotCreateDirectoryForExistingMapping() throws IOException {
      Files.delete(basePath.resolve("existingId_1"));

      Path path = resolverWithExistingData.create(Path.class).getLocation("existingId_1");

      assertThat(path).doesNotExist();
    }
  }

  private String getXmlFileContent() {
    Path storePath = basePath.resolve("config").resolve("repository-paths.xml");

    assertThat(storePath).isRegularFile();
    return content(storePath);
  }

  private PathBasedRepositoryLocationResolver createResolver() {
    return new PathBasedRepositoryLocationResolver(contextProvider, initialRepositoryLocationResolver, fileSystem, clock);
  }

  private String content(Path storePath) {
    try {
      return new String(Files.readAllBytes(storePath), Charsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
