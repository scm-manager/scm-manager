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

package sonia.scm.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepositoryReadOnlyCheckerTest {

  private final Repository repository = new Repository("1", "git", "hitchhiker", "HeartOfGold");

  private boolean archived = false;
  private boolean exporting = false;

  private final RepositoryArchivedCheck archivedCheck = repositoryId -> archived;
  private final RepositoryExportingCheck exportingCheck = new RepositoryExportingCheck() {
    @Override
    public boolean isExporting(String repositoryId) {
      return exporting;
    }

    @Override
    public <T> T withExportingLock(Repository repository, Supplier<T> callback) {
      return null;
    }
  };

  @BeforeEach
  void resetStates() {
    archived = false;
    exporting = false;
  }

  @AfterEach
  void unregisterStaticChecks() {
    RepositoryReadOnlyChecker.setReadOnlyChecks(Collections.emptySet());
  }

  @Nested
  class LegacyChecks {

    private final RepositoryReadOnlyChecker checker = new RepositoryReadOnlyChecker(archivedCheck, exportingCheck);

    @Test
    void shouldReturnFalseIfAllChecksFalse() {
      boolean readOnly = checker.isReadOnly(repository);

      assertThat(readOnly).isFalse();
    }

    @Test
    void shouldReturnTrueIfArchivedIsTrue() {
      archived = true;

      boolean readOnly = checker.isReadOnly(repository);

      assertThat(readOnly).isTrue();
    }

    @Test
    void shouldReturnTrueIfExportingIsTrue() {
      exporting = true;

      boolean readOnly = checker.isReadOnly(repository);

      assertThat(readOnly).isTrue();
    }

    @Test
    void shouldHandleLegacyAndStatic() {
      assertThat(checker.isReadOnly(repository)).isFalse();

      RepositoryReadOnlyChecker.setReadOnlyChecks(Collections.singleton(new SampleReadOnlyCheck(true)));
      assertThat(checker.isReadOnly(repository)).isTrue();

      RepositoryReadOnlyChecker.setReadOnlyChecks(Collections.emptySet());
      assertThat(checker.isReadOnly(repository)).isFalse();

      exporting = true;
      assertThat(checker.isReadOnly(repository)).isTrue();
    }

  }

  @Nested
  class StaticChecks {

    private final RepositoryReadOnlyChecker checker = new RepositoryReadOnlyChecker();

    @BeforeEach
    void registerStaticChecks() {
      RepositoryReadOnlyChecker.setReadOnlyChecks(Arrays.asList(
        archivedCheck, exportingCheck
      ));
    }

    @Test
    void shouldReturnFalseIfAllChecksFalse() {
      boolean readOnly = checker.isReadOnly(repository);

      assertThat(readOnly).isFalse();
    }

    @Test
    void shouldReturnTrueIfArchivedIsTrue() {
      archived = true;

      boolean readOnly = checker.isReadOnly(repository);

      assertThat(readOnly).isTrue();
    }

    @Test
    void shouldReturnTrueIfExportingIsTrue() {
      exporting = true;

      boolean readOnly = checker.isReadOnly(repository);

      assertThat(readOnly).isTrue();
    }

    @Test
    void shouldThrowReadOnlyException() {
      exporting = true;

      assertThrows(ReadOnlyException.class, () -> RepositoryReadOnlyChecker.checkReadOnly(repository));
    }

  }

  private static class SampleReadOnlyCheck implements ReadOnlyCheck {

    private final boolean readOnly;

    public SampleReadOnlyCheck(boolean readOnly) {
      this.readOnly = readOnly;
    }

    @Override
    public String getReason() {
      return "testing purposes";
    }

    @Override
    public boolean isReadOnly(String repositoryId) {
      return readOnly;
    }
  }

}
