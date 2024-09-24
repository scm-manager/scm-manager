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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryArchivedCheckTest {

  private final RepositoryArchivedCheck check = new TestingRepositoryArchivedCheck();

  private final Repository repository = new Repository("42", "git", "hitchhiker", "hog");

  @Test
  void shouldThrowForRepositoryMarkedAsArchived() {
    assertThrows(RepositoryArchivedException.class, () -> check.check(repository));
  }

  @Test
  void shouldThrowForRepositoryMarkedAsArchivedWithId() {
    assertThrows(RepositoryArchivedException.class, () -> check.check("42"));
  }

  @Test
  void shouldThrowForArchivedRepository() {
    Repository repository = new Repository("21", "hg", "hitchhiker", "puzzle42");
    repository.setArchived(true);
    assertThrows(RepositoryArchivedException.class, () -> check.check(repository));
  }

  private static class TestingRepositoryArchivedCheck implements RepositoryArchivedCheck {
    @Override
    public boolean isArchived(String repositoryId) {
      return "42".equals(repositoryId);
    }
  }

}
