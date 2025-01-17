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
import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.io.DefaultFileSystem;
import sonia.scm.io.FileSystem;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryExportingCheck;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.xml.PathBasedRepositoryLocationResolver.DownForMaintenanceContext;
import sonia.scm.repository.xml.PathBasedRepositoryLocationResolver.UpAfterMaintenanceContext;
import sonia.scm.store.StoreReadOnlyException;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class XmlRepositoryDAOTest {

  private final Repository REPOSITORY = createRepository("42");

  @Mock
  private PathBasedRepositoryLocationResolver locationResolver;
  private Consumer<BiConsumer<String, Path>> triggeredOnForAllLocations = none -> {};
  @Mock
  private RepositoryExportingCheck repositoryExportingCheck;

  private final FileSystem fileSystem = new DefaultFileSystem();

  private XmlRepositoryDAO dao;

  @BeforeEach
  void createDAO(@TempDir Path basePath) {
    when(locationResolver.create(Path.class)).thenReturn(
      new RepositoryLocationResolver.RepositoryLocationResolverInstance<>() {
        @Override
        public Path getLocation(String repositoryId) {
          return locationResolver.create(repositoryId);
        }

        @Override
        public Path createLocation(String repositoryId) {
          return locationResolver.create(repositoryId);
        }

        @Override
        public void setLocation(String repositoryId, Path location) {
        }

        @Override
        public void forAllLocations(BiConsumer<String, Path> consumer) {
          triggeredOnForAllLocations.accept(consumer);
        }
      }
    );
    when(locationResolver.create(any(Repository.class))).thenAnswer(invocation -> createMockedRepoPath(basePath, invocation.getArgument(0, Repository.class).getId()));
    when(locationResolver.create(anyString())).thenAnswer(invocation -> createMockedRepoPath(basePath, invocation.getArgument(0, String.class)));
    when(locationResolver.remove(anyString())).thenAnswer(invocation -> basePath.resolve(invocation.getArgument(0).toString()));
  }

  private static Path createMockedRepoPath(Path basePath, String repositoryId) {
    Path resolvedPath = basePath.resolve(repositoryId);
    try {
      Files.createDirectories(resolvedPath);
    } catch (IOException e) {
      fail(e);
    }
    return resolvedPath;
  }

  @Nested
  class WithEmptyDatabase {

    @BeforeEach
    void createDAO() {
      dao = new XmlRepositoryDAO(locationResolver, fileSystem, repositoryExportingCheck);
    }

    @Test
    void shouldReturnXmlType() {
      assertThat(dao.getType()).isEqualTo("xml");
    }

    @Test
    void shouldReturnCreationTimeOfLocationResolver() {
      long now = 42L;
      when(locationResolver.getCreationTime()).thenReturn(now);
      assertThat(dao.getCreationTime()).isEqualTo(now);
    }

    @Test
    void shouldReturnLasModifiedOfLocationResolver() {
      long now = 42L;
      when(locationResolver.getLastModified()).thenReturn(now);
      assertThat(dao.getLastModified()).isEqualTo(now);
    }

    @Test
    void shouldReturnTrueForEachContainsMethod() {
      dao.add(REPOSITORY);

      assertThat(dao.contains(REPOSITORY)).isTrue();
      assertThat(dao.contains(REPOSITORY.getId())).isTrue();
      assertThat(dao.contains(REPOSITORY.getNamespaceAndName())).isTrue();
    }

    @Test
    void shouldPersistRepository() {
      dao.add(REPOSITORY);

      String content = getXmlFileContent(REPOSITORY.getId());

      assertThat(content).contains("<id>42</id>");
    }

    @Test
    void shouldDeleteDataFile() {
      dao.add(REPOSITORY);
      dao.delete(REPOSITORY);

      assertThat(metadataFile(REPOSITORY.getId())).doesNotExist();
    }

    @Test
    void shouldModifyRepository() {
      dao.add(REPOSITORY);
      Repository changedRepository = REPOSITORY.clone();
      changedRepository.setContact("change");

      dao.modify(changedRepository);

      String content = getXmlFileContent(REPOSITORY.getId());

      assertThat(content).contains("change");
    }

    @Test
    void shouldReturnFalseForEachContainsMethod() {
      assertThat(dao.contains(REPOSITORY)).isFalse();
      assertThat(dao.contains(REPOSITORY.getId())).isFalse();
      assertThat(dao.contains(REPOSITORY.getNamespaceAndName())).isFalse();
    }

    @Test
    void shouldReturnNullForEachGetMethod() {
      assertThat(dao.get("42")).isNull();
      assertThat(dao.get(new NamespaceAndName("hitchhiker", "HeartOfGold"))).isNull();
    }

    @Test
    void shouldReturnRepository() {
      dao.add(REPOSITORY);

      assertThat(dao.get("42")).isEqualTo(REPOSITORY);
      assertThat(dao.get(new NamespaceAndName("space", "42"))).isEqualTo(REPOSITORY);
    }

    @Test
    void shouldNotReturnTheSameInstance() {
      dao.add(REPOSITORY);

      Repository repository = dao.get("42");
      assertThat(repository).isNotSameAs(REPOSITORY);
    }

    @Test
    void shouldReturnAllRepositories() {
      dao.add(REPOSITORY);

      Repository secondRepository = createRepository("23");
      dao.add(secondRepository);

      Collection<Repository> repositories = dao.getAll();
      assertThat(repositories)
        .containsExactlyInAnyOrder(REPOSITORY, secondRepository);
    }

    @Test
    void shouldModifyRepositoryTwice() {
      REPOSITORY.setDescription("HeartOfGold");
      dao.add(REPOSITORY);
      assertThat(dao.get("42").getDescription()).isEqualTo("HeartOfGold");

      Repository heartOfGold = createRepository("42");
      heartOfGold.setDescription("Heart of Gold");
      dao.modify(heartOfGold);

      assertThat(dao.get("42").getDescription()).isEqualTo("Heart of Gold");
    }

    @Test
    void shouldNotModifyArchivedRepository() {
      REPOSITORY.setArchived(true);
      dao.add(REPOSITORY);

      Repository heartOfGold = createRepository("42");
      heartOfGold.setArchived(true);
      assertThrows(StoreReadOnlyException.class, () -> dao.modify(heartOfGold));
    }

    @Test
    void shouldNotModifyExportingRepository() {
      when(repositoryExportingCheck.isExporting(REPOSITORY)).thenReturn(true);
      dao.add(REPOSITORY);

      Repository heartOfGold = createRepository("42");
      assertThrows(StoreReadOnlyException.class, () -> dao.modify(heartOfGold));
    }

    @Test
    void shouldRemoveRepository() {
      dao.add(REPOSITORY);
      assertThat(dao.contains("42")).isTrue();

      dao.delete(REPOSITORY);
      assertThat(dao.contains("42")).isFalse();
      assertThat(dao.contains(REPOSITORY.getNamespaceAndName())).isFalse();

      Path storePath = metadataFile(REPOSITORY.getId());

      assertThat(storePath).doesNotExist();
    }

    @Test
    void shouldNotRemoveArchivedRepository() {
      REPOSITORY.setArchived(true);
      dao.add(REPOSITORY);
      assertThat(dao.contains("42")).isTrue();

      assertThrows(StoreReadOnlyException.class, () -> dao.delete(REPOSITORY));
    }

    @Test
    void shouldNotRemoveExportingRepository() {
      when(repositoryExportingCheck.isExporting(REPOSITORY)).thenReturn(true);
      dao.add(REPOSITORY);
      assertThat(dao.contains("42")).isTrue();

      assertThrows(StoreReadOnlyException.class, () -> dao.delete(REPOSITORY));
    }

    @Test
    void shouldRenameTheRepository() {
      dao.add(REPOSITORY);

      REPOSITORY.setNamespace("hg2tg");
      REPOSITORY.setName("hog");

      dao.modify(REPOSITORY);

      Repository repository = dao.get("42");
      assertThat(repository.getNamespace()).isEqualTo("hg2tg");
      assertThat(repository.getName()).isEqualTo("hog");

      assertThat(dao.contains(new NamespaceAndName("hg2tg", "hog"))).isTrue();
      assertThat(dao.contains(new NamespaceAndName("hitchhiker", "HeartOfGold"))).isFalse();

      String content = getXmlFileContent(REPOSITORY.getId());
      assertThat(content).contains("<name>hog</name>");
    }

    @Test
    void shouldDeleteRepositoryEvenWithChangedNamespace() {
      dao.add(REPOSITORY);

      REPOSITORY.setNamespace("hg2tg");
      REPOSITORY.setName("hog");

      dao.delete(REPOSITORY);

      assertThat(dao.contains(new NamespaceAndName("space", "42"))).isFalse();
    }

    @Test
    void shouldRemoveRepositoryDirectoryAfterDeletion() {
      dao.add(REPOSITORY);

      Path path = locationResolver.create(REPOSITORY.getId());
      assertThat(path).isDirectory();

      dao.delete(REPOSITORY);
      assertThat(path).doesNotExist();
    }

    @Test
    void shouldPersistPermissions() {
      REPOSITORY.setPermissions(asList(new RepositoryPermission("trillian", asList("read", "write"), false), new RepositoryPermission("vogons", singletonList("delete"), true)));
      dao.add(REPOSITORY);

      String content = getXmlFileContent(REPOSITORY.getId());
      assertThat(content)
        .containsSubsequence("trillian", "<verb>read</verb>", "<verb>write</verb>")
        .containsSubsequence("vogons", "<verb>delete</verb>");
    }

    @Test
    void shouldUpdateRepositoryPathDatabse() {
      dao.add(REPOSITORY);

      verify(locationResolver, never()).updateModificationDate();

      dao.modify(REPOSITORY);

      verify(locationResolver).updateModificationDate();
    }

    private String getXmlFileContent(String id) {
      Path storePath = metadataFile(id);

      assertThat(storePath).isRegularFile();
      return content(storePath);
    }

    private Path metadataFile(String id) {
      return locationResolver.create(id).resolve("metadata.xml");
    }

    private String content(Path storePath) {
      try {
        return Files.readString(storePath, Charsets.UTF_8);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Nested
  class WithExistingRepositories {

    private Path repositoryPath;
    @Captor
    private ArgumentCaptor<PathBasedRepositoryLocationResolver.MaintenanceCallback> callbackArgumentCaptor;

    @BeforeEach
    void createMetadataFileForRepository(@TempDir Path basePath) throws IOException {
      repositoryPath = basePath.resolve("existing");

      prepareRepositoryPath(repositoryPath);
    }

    @Test
    void shouldReadExistingRepositoriesFromPathDatabase() {
      // given
      mockExistingPath();

      // when
      XmlRepositoryDAO dao = new XmlRepositoryDAO(locationResolver, fileSystem, repositoryExportingCheck);

      // then
      assertThat(dao.contains(new NamespaceAndName("space", "existing"))).isTrue();
    }

    @Test
    void shouldRefreshWithExistingRepositoriesFromPathDatabase() {
      // given
      mockExistingPath();

      XmlRepositoryDAO dao = new XmlRepositoryDAO(locationResolver, fileSystem, repositoryExportingCheck);

      // when
      dao.refresh();

      // then
      verify(locationResolver).refresh();
      assertThat(dao.contains(new NamespaceAndName("space", "existing"))).isTrue();
    }

    @Test
    void shouldHandleMaintenanceEvents() {
      doNothing().when(locationResolver).registerMaintenanceCallback(callbackArgumentCaptor.capture());
      mockExistingPath();

      XmlRepositoryDAO dao = new XmlRepositoryDAO(locationResolver, fileSystem, repositoryExportingCheck);

      callbackArgumentCaptor.getValue().downForMaintenance(new DownForMaintenanceContext("existing"));

      assertThat(dao.contains("existing")).isFalse();
      assertThat(dao.contains(new NamespaceAndName("space", "existing"))).isFalse();
      assertThat(dao.getAll()).isEmpty();

      callbackArgumentCaptor.getValue().upAfterMaintenance(new UpAfterMaintenanceContext("existing", repositoryPath));

      assertThat(dao.contains("existing")).isTrue();
      assertThat(dao.contains(new NamespaceAndName("space", "existing"))).isTrue();
      assertThat(dao.getAll()).hasSize(1);
    }

    private void mockExistingPath() {
      triggeredOnForAllLocations = consumer -> consumer.accept("existing", repositoryPath);
    }
  }

  @Nested
  class WithDuplicateRepositories {
    private Path repositoryPath;
    private Path duplicateRepositoryPath;

    @BeforeEach
    void createMetadataFileForRepository(@TempDir Path basePath) throws IOException {
      repositoryPath = basePath.resolve("existing");
      duplicateRepositoryPath = basePath.resolve("duplicate");

      prepareRepositoryPath(repositoryPath);
      prepareRepositoryPath(duplicateRepositoryPath);
    }

    @Test
    void shouldRenameDuplicateRepositories() {
      mockExistingPath();

      XmlRepositoryDAO dao = new XmlRepositoryDAO(locationResolver, fileSystem, repositoryExportingCheck);

      assertThat(dao.contains(new NamespaceAndName("space", "existing"))).isTrue();
      assertThat(dao.contains(new NamespaceAndName("space", "existing-existing2-DUPLICATE"))).isTrue();
    }

    private void mockExistingPath() {
      triggeredOnForAllLocations = consumer -> {
        consumer.accept("existing", repositoryPath);
        consumer.accept("existing2", duplicateRepositoryPath);
      };
    }
  }

  private void prepareRepositoryPath(Path repositoryPath) throws IOException {
    Files.createDirectories(repositoryPath);
    URL metadataUrl = Resources.getResource("sonia/scm/store/repositoryDaoMetadata.xml");
    Files.copy(metadataUrl.openStream(), repositoryPath.resolve("metadata.xml"));
  }

  private Repository createRepository(String id) {
    return new Repository(id, "xml", "space", id);
  }
}
