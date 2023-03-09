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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.JAXBConfigurationStoreFactory;

import java.nio.file.Path;
import java.util.Optional;

import static java.util.Collections.emptySet;
import static org.mockito.Mockito.when;
import static sonia.scm.update.repository.MigrationStrategy.INLINE;

@ExtendWith(MockitoExtension.class)
class DefaultMigrationStrategyDAOTest {

  @Mock
  SCMContextProvider contextProvider;

  private ConfigurationStoreFactory storeFactory;

  @BeforeEach
  void initStore(@TempDir Path tempDir) {
    when(contextProvider.getBaseDirectory()).thenReturn(tempDir.toFile());
    storeFactory = new JAXBConfigurationStoreFactory(contextProvider, null, null, emptySet());
  }

  @Test
  void shouldReturnEmptyOptionalWhenStoreIsEmpty() {
    DefaultMigrationStrategyDAO dao = new DefaultMigrationStrategyDAO(storeFactory);

    Optional<RepositoryMigrationPlan.RepositoryMigrationEntry> entry = dao.get("any");

    Assertions.assertThat(entry).isEmpty();
  }

  @Test
  void shouldReturnNewValue() {
    DefaultMigrationStrategyDAO dao = new DefaultMigrationStrategyDAO(storeFactory);

    dao.set("id", "git", "originalName", INLINE, "space", "name");

    Optional<RepositoryMigrationPlan.RepositoryMigrationEntry> entry = dao.get("id");

    Assertions.assertThat(entry)
      .map(RepositoryMigrationPlan.RepositoryMigrationEntry::getDataMigrationStrategy)
      .contains(INLINE);
    Assertions.assertThat(entry)
      .map(RepositoryMigrationPlan.RepositoryMigrationEntry::getNewNamespace)
      .contains("space");
    Assertions.assertThat(entry)
      .map(RepositoryMigrationPlan.RepositoryMigrationEntry::getNewName)
      .contains("name");
  }

  @Nested
  class WithExistingDatabase {
    @BeforeEach
    void initExistingDatabase() {
      DefaultMigrationStrategyDAO dao = new DefaultMigrationStrategyDAO(storeFactory);

      dao.set("id", "git", "originalName", INLINE, "space", "name");
    }

    @Test
    void shouldFindExistingValue() {
      DefaultMigrationStrategyDAO dao = new DefaultMigrationStrategyDAO(storeFactory);

      Optional<RepositoryMigrationPlan.RepositoryMigrationEntry> entry = dao.get("id");

      Assertions.assertThat(entry)
        .map(RepositoryMigrationPlan.RepositoryMigrationEntry::getDataMigrationStrategy)
        .contains(INLINE);
      Assertions.assertThat(entry)
        .map(RepositoryMigrationPlan.RepositoryMigrationEntry::getNewNamespace)
        .contains("space");
      Assertions.assertThat(entry)
        .map(RepositoryMigrationPlan.RepositoryMigrationEntry::getNewName)
        .contains("name");
      Assertions.assertThat(entry)
        .map(RepositoryMigrationPlan.RepositoryMigrationEntry::getOriginalName)
        .contains("originalName");
    }
  }
}
