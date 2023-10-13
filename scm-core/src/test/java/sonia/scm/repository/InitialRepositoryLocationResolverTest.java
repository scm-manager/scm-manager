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
