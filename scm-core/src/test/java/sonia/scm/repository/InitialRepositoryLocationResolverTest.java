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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({MockitoExtension.class})
class InitialRepositoryLocationResolverTest {

  private final InitialRepositoryLocationResolver resolver = new InitialRepositoryLocationResolver(emptySet());

  @Test
  void shouldComputeInitialPath() {
    Path path = resolver.getPath("42");

    assertThat(path)
      .isRelative()
      .hasToString("repositories" + File.separator + "42");
  }

  @Test
  void shouldThrowIllegalArgumentExceptionIfIdHasASlash() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> resolver.getPath("../../../passwd"));
  }

  @Test
  void shouldThrowIllegalArgumentExceptionIfIdHasABackSlash() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> resolver.getPath("..\\..\\..\\users.ntlm"));
  }

  @Test
  void shouldThrowIllegalArgumentExceptionIfIdIsDotDot() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> resolver.getPath(".."));
  }

  @Test
  void shouldThrowIllegalArgumentExceptionIfIdIsDot() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> resolver.getPath("."));
  }

  @Test
  void shouldUseOverrideForRepository() {
    InitialRepositoryLocationResolver resolver = new InitialRepositoryLocationResolver(
      singleton((repository, defaultPath) -> defaultPath.resolve(repository.getId()))
    );
    Path path = resolver.getPath(new Repository("42", "git", "space", "X"));

    assertThat(path)
      .isRelative()
      .hasToString("repositories" + File.separator + "42" + File.separator + "42");
  }
}
