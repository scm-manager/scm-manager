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

package sonia.scm.repository.xml;

import com.google.common.base.Charsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.SCMContextProvider;
import sonia.scm.io.DefaultFileSystem;
import sonia.scm.io.FileSystem;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryLocationResolver.RepositoryLocationResolverInstance;
import sonia.scm.repository.xml.PathBasedRepositoryLocationResolver.DownForMaintenanceContext;
import sonia.scm.repository.xml.PathBasedRepositoryLocationResolver.UpAfterMaintenanceContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
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
  void shouldFailIfDirectoryExists() throws IOException {
    Files.createDirectories(basePath.resolve("newId"));

    RepositoryLocationResolverInstance<Path> resolverInstance = resolver.forClass(Path.class);

    assertThatThrownBy(() -> resolverInstance.createLocation("newId"))
      .isInstanceOf(RepositoryLocationResolver.RepositoryStorageException.class);
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

    @Spy
    private PathBasedRepositoryLocationResolver.MaintenanceCallback maintenanceCallback;

    @BeforeEach
    void createExistingDatabase() {
      resolver.forClass(Path.class).createLocation("existingId_1");
      resolver.forClass(Path.class).createLocation("existingId_2");
      resolverWithExistingData = createResolver();
      resolverWithExistingData.registerMaintenanceCallback(maintenanceCallback);
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

    @Test
    void shouldModifyLocation() throws IOException {
      Path oldPath = resolverWithExistingData.create(Path.class).getLocation("existingId_1");
      Path newPath = basePath.resolve("modified_location");

      resolverWithExistingData.create(Path.class).modifyLocation("existingId_1", newPath);

      assertThat(newPath).exists();
      assertThat(oldPath).doesNotExist();
      assertThat(resolverWithExistingData.create(Path.class).getLocation("existingId_1")).isEqualTo(newPath);
      verify(maintenanceCallback).downForMaintenance(new DownForMaintenanceContext("existingId_1"));
      verify(maintenanceCallback).upAfterMaintenance(new UpAfterMaintenanceContext("existingId_1", newPath));
    }

    @Test
    void shouldModifyLocationAndKeepOld() throws IOException {
      Path oldPath = resolverWithExistingData.create(Path.class).getLocation("existingId_1");
      Path newPath = basePath.resolve("modified_location");

      resolverWithExistingData.create(Path.class).modifyLocationAndKeepOld("existingId_1", newPath);

      assertThat(newPath).exists();
      assertThat(oldPath).exists();
      assertThat(resolverWithExistingData.create(Path.class).getLocation("existingId_1")).isEqualTo(newPath);
      verify(maintenanceCallback).downForMaintenance(new DownForMaintenanceContext("existingId_1"));
      verify(maintenanceCallback).upAfterMaintenance(new UpAfterMaintenanceContext("existingId_1", newPath));
    }

    @Test
    void shouldHandleErrorOnModifyLocation() throws IOException {
      Path oldPath = resolverWithExistingData.create(Path.class).getLocation("existingId_1");
      Path newPath = basePath.resolve("thou").resolve("shall").resolve("not").resolve("move").resolve("here");
      Files.createDirectories(newPath);
      Files.setPosixFilePermissions(newPath, Set.of(PosixFilePermission.OWNER_READ));

      assertThatThrownBy(() -> resolverWithExistingData.create(Path.class).modifyLocationAndKeepOld("existingId_1", newPath))
        .isInstanceOf(RepositoryLocationResolver.RepositoryStorageException.class);

      assertThat(newPath).exists();
      assertThat(oldPath).exists();
      assertThat(resolverWithExistingData.create(Path.class).getLocation("existingId_1")).isEqualTo(oldPath);
      verify(maintenanceCallback).downForMaintenance(new DownForMaintenanceContext("existingId_1"));
      verify(maintenanceCallback).upAfterMaintenance(new UpAfterMaintenanceContext("existingId_1", oldPath));
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
