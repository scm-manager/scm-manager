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

package sonia.scm.update.repository;

import com.google.common.io.Resources;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.xml.XmlRepositoryDAO;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.InMemoryConfigurationEntryStoreFactory;
import sonia.scm.update.UpdateStepTestUtil;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.update.repository.MigrationStrategy.MOVE;

@ExtendWith(MockitoExtension.class)
class XmlRepositoryV1UpdateStepTest {

  Injector injectorMock = MigrationStrategyMock.init();

  @Mock
  XmlRepositoryDAO repositoryDAO;
  @Mock
  DefaultMigrationStrategyDAO migrationStrategyDao;

  InMemoryConfigurationEntryStoreFactory configurationEntryStoreFactory = new InMemoryConfigurationEntryStoreFactory();

  @Captor
  ArgumentCaptor<Repository> storeCaptor;
  @Captor
  ArgumentCaptor<Path> locationCaptor;

  UpdateStepTestUtil testUtil;

  XmlRepositoryV1UpdateStep updateStep;

  @BeforeEach
  void createUpdateStepFromMocks(@TempDir Path tempDir) {
    testUtil = new UpdateStepTestUtil(tempDir);
    updateStep = new XmlRepositoryV1UpdateStep(
      testUtil.getContextProvider(),
      repositoryDAO,
      migrationStrategyDao,
      injectorMock,
      configurationEntryStoreFactory
    );
  }

  @Nested
  class WithExistingDatabase {

    @BeforeEach
    void createV1Home(@TempDir Path tempDir) throws IOException {
      V1RepositoryFileSystem.createV1Home(tempDir);
    }

    @BeforeEach
    void captureStoredRepositories() {
      lenient().doNothing().when(repositoryDAO).add(storeCaptor.capture(), locationCaptor.capture());
    }

    @BeforeEach
    void createMigrationPlan() {
      Answer<Object> planAnswer = invocation -> {
        String id = invocation.getArgument(0).toString();
        return of(new RepositoryMigrationPlan.RepositoryMigrationEntry(id, "git", "originalName", MOVE, "namespace-" + id, "name-" + id));
      };

      lenient().when(migrationStrategyDao.get("3b91caa5-59c3-448f-920b-769aaa56b761")).thenAnswer(planAnswer);
      lenient().when(migrationStrategyDao.get("c1597b4f-a9f0-49f7-ad1f-37d3aae1c55f")).thenAnswer(planAnswer);
      lenient().when(migrationStrategyDao.get("454972da-faf9-4437-b682-dc4a4e0aa8eb")).thenAnswer(planAnswer);
    }

    @Test
    void shouldCreateNewRepositories() throws JAXBException {
      updateStep.doUpdate();
      verify(repositoryDAO, times(3)).add(any(), any());
    }

    @Test
    void shouldMapAttributes() throws JAXBException {
      updateStep.doUpdate();

      Optional<Repository> repository = findByNamespace("namespace-3b91caa5-59c3-448f-920b-769aaa56b761");

      assertThat(repository)
        .get()
        .hasFieldOrPropertyWithValue("type", "git")
        .hasFieldOrPropertyWithValue("contact", "arthur@dent.uk")
        .hasFieldOrPropertyWithValue("description", "A repository with two folders.")
        .hasFieldOrPropertyWithValue("archived", false);
    }

    @Test
    void shouldMapArchivedAttribute() throws JAXBException {
      updateStep.doUpdate();

      Optional<Repository> repository = findByNamespace("namespace-c1597b4f-a9f0-49f7-ad1f-37d3aae1c55f");

      assertThat(repository)
        .get()
        .hasFieldOrPropertyWithValue("archived", true);
    }

    @Test
    void shouldMapPermissions() throws JAXBException {
      updateStep.doUpdate();

      Optional<Repository> repository = findByNamespace("namespace-454972da-faf9-4437-b682-dc4a4e0aa8eb");

      assertThat(repository.get().getPermissions())
        .hasSize(3)
        .contains(
          new RepositoryPermission("mice", "WRITE", true),
          new RepositoryPermission("dent", "OWNER", false),
          new RepositoryPermission("trillian", "READ", false)
        );
    }

    @Test
    void shouldExtractPropertiesFromRepositories() throws JAXBException {
      updateStep.doUpdate();

      ConfigurationEntryStore store = configurationEntryStoreFactory.get("repository-properties-v1");
      assertThat(store.getAll())
        .hasSize(3);
    }

    @Test
    void shouldUseDirectoryFromStrategy(@TempDir Path tempDir) throws JAXBException {
      Path targetDir = tempDir.resolve("someDir");
      MigrationStrategy.Instance strategyMock = injectorMock.getInstance(MoveMigrationStrategy.class);
      when(strategyMock.migrate("454972da-faf9-4437-b682-dc4a4e0aa8eb", "simple", "git")).thenReturn(of(targetDir));

      updateStep.doUpdate();

      assertThat(locationCaptor.getAllValues()).contains(targetDir);
    }

    @Test
    void shouldSkipWhenStrategyGivesNoNewPath() throws JAXBException {
      for (MigrationStrategy strategy : MigrationStrategy.values()) {
        MigrationStrategy.Instance strategyMock = mock(strategy.getImplementationClass());
        lenient().when(strategyMock.migrate(any(), any(), any())).thenReturn(empty());
        lenient().when(injectorMock.getInstance((Class<MigrationStrategy.Instance>) strategy.getImplementationClass())).thenReturn(strategyMock);
      }

      updateStep.doUpdate();

      assertThat(locationCaptor.getAllValues()).isEmpty();
    }

    @Test
    void shouldFailForMissingMigrationStrategy() {
      lenient().when(migrationStrategyDao.get("c1597b4f-a9f0-49f7-ad1f-37d3aae1c55f")).thenReturn(empty());
      assertThrows(IllegalStateException.class, () -> updateStep.doUpdate());
    }

    @Test
    void shouldBackupOldRepositoryDatabaseFile(@TempDir Path tempDir) throws JAXBException {
      updateStep.doUpdate();

      assertThat(tempDir.resolve("config").resolve("repositories.xml")).doesNotExist();
      assertThat(tempDir.resolve("config").resolve("repositories.xml.v1.backup")).exists();
    }
  }

  @Test
  void shouldNotFailIfNoOldDatabaseExists() throws JAXBException {
    updateStep.doUpdate();
  }

  @Test
  void shouldNotFailIfFormerV2DatabaseExists(@TempDir Path tempDir) throws JAXBException, IOException {
    createFormerV2RepositoriesFile(tempDir);

    updateStep.doUpdate();
  }

  @Test
  void shouldNotBackupFormerV2DatabaseFile(@TempDir Path tempDir) throws JAXBException, IOException {
    createFormerV2RepositoriesFile(tempDir);

    updateStep.doUpdate();

    assertThat(tempDir.resolve("config").resolve("repositories.xml")).exists();
    assertThat(tempDir.resolve("config").resolve("repositories.xml.v1.backup")).doesNotExist();
  }

  @Test
  void shouldGetNoMissingStrategiesWithFormerV2DatabaseFile(@TempDir Path tempDir) throws IOException {
    createFormerV2RepositoriesFile(tempDir);

    assertThat(updateStep.getRepositoriesWithoutMigrationStrategies()).isEmpty();
  }

  @Test
  void shouldFindMissingStrategies(@TempDir Path tempDir) throws IOException {
    V1RepositoryFileSystem.createV1Home(tempDir);

    assertThat(updateStep.getRepositoriesWithoutMigrationStrategies())
      .extracting("id")
      .contains(
        "3b91caa5-59c3-448f-920b-769aaa56b761",
        "c1597b4f-a9f0-49f7-ad1f-37d3aae1c55f",
        "454972da-faf9-4437-b682-dc4a4e0aa8eb");
  }

  private void createFormerV2RepositoriesFile(@TempDir Path tempDir) throws IOException {
    URL url = Resources.getResource("sonia/scm/update/repository/formerV2RepositoryFile.xml");
    Path configDir = tempDir.resolve("config");
    Files.createDirectories(configDir);
    Files.copy(url.openStream(), configDir.resolve("repositories.xml"));
  }

  private Optional<Repository> findByNamespace(String namespace) {
    return storeCaptor.getAllValues().stream().filter(r -> r.getNamespace().equals(namespace)).findFirst();
  }
}
