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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sonia.scm.ScmConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomNamespaceStrategyTest {

  private final NamespaceStrategy namespaceStrategy = new CustomNamespaceStrategy();

  @ParameterizedTest
  @ValueSource(strings = {"valid", "123_", "something_valid", "1234", "create_it"})
  void shouldReturnNamespaceFromRepository(String expectedValidNamespace) {
    Repository repository = RepositoryTestData.createHeartOfGold();
    repository.setNamespace(expectedValidNamespace);

    assertThat(namespaceStrategy.createNamespace(repository)).isEqualTo(expectedValidNamespace);
  }

  @ParameterizedTest
  @ValueSource(strings = {"..", " ", "0", "123", "create"})
  void shouldThrowAnValidationExceptionForAnInvalidNamespace(String expectedAsInvalidNamespace) {
    Repository repository = RepositoryTestData.createHeartOfGold();
    repository.setNamespace(expectedAsInvalidNamespace);

    assertThrows(ScmConstraintViolationException.class, () -> namespaceStrategy.createNamespace(repository));
  }
}
