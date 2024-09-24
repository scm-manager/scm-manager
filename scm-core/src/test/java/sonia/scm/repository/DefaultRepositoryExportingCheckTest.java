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

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultRepositoryExportingCheckTest {

  private static final Repository EXPORTING_REPOSITORY = new Repository("exporting_hog", "git", "hitchhiker", "hog");

  private final DefaultRepositoryExportingCheck check = new DefaultRepositoryExportingCheck();

  @Test
  void shouldBeReadOnlyIfBeingExported() {
    check.withExportingLock(EXPORTING_REPOSITORY, () -> {
      boolean readOnly = check.isExporting(EXPORTING_REPOSITORY);
      assertThat(readOnly).isTrue();
      return null;
    });
  }

  @Test
  void shouldBeReadOnlyIfBeingExportedMultipleTimes() {
    check.withExportingLock(EXPORTING_REPOSITORY, () -> {
      check.withExportingLock(EXPORTING_REPOSITORY, () -> {
        boolean readOnly = check.isExporting(EXPORTING_REPOSITORY);
        assertThat(readOnly).isTrue();
        return null;
      });
      boolean readOnly = check.isExporting(EXPORTING_REPOSITORY);
      assertThat(readOnly).isTrue();
      return null;
    });
  }

  @Test
  void shouldNotBeReadOnlyIfNotBeingExported() {
    boolean readOnly = check.isExporting(EXPORTING_REPOSITORY);
    assertThat(readOnly).isFalse();
  }

  @Test
  void shouldThrowExportingException() {
    RepositoryExportingCheck check = new TestingRepositoryExportingCheck();

    assertThrows(RepositoryExportingException.class, () -> check.check(EXPORTING_REPOSITORY));
  }

  @Test
  void shouldThrowExportingExceptionWithId() {
    RepositoryExportingCheck check = new TestingRepositoryExportingCheck();

    assertThrows(RepositoryExportingException.class, () -> check.check("exporting_hog"));
  }

  private static class TestingRepositoryExportingCheck implements RepositoryExportingCheck {


    @Override
    public boolean isExporting(String repositoryId) {
      return "exporting_hog".equals(repositoryId);
    }

    @Override
    public <T> T withExportingLock(Repository repository, Supplier<T> callback) {
      return null;
    }
  }
}
