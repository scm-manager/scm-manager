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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ReadOnlyCheckTest {

  private final ReadOnlyCheck check = new TestingReadOnlyCheck();

  private final Repository repository = new Repository("42", "git", "hitchhiker", "hog");

  @Test
  void shouldDelegateToMethodWithRepositoryId() {
    assertThat(check.isReadOnly(repository)).isTrue();
  }

  @Test
  void shouldThrowReadOnlyException() {
    assertThrows(ReadOnlyException.class, () -> check.check(repository));
  }

  @Test
  void shouldThrowReadOnlyExceptionForId() {
    assertThrows(ReadOnlyException.class, () -> check.check("42"));
  }

  @Test
  void shouldNotThrowException() {
    assertDoesNotThrow(() -> check.check("21"));
  }

  @Test
  void shouldDelegateToNormalCheck() {
    assertThat(check.isReadOnly("any", "42")).isTrue();
  }

  private class TestingReadOnlyCheck implements ReadOnlyCheck {

    @Override
    public String getReason() {
      return "Testing";
    }

    @Override
    public boolean isReadOnly(String repositoryId) {
      return repositoryId.equals("42");
    }
  }

}
