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
