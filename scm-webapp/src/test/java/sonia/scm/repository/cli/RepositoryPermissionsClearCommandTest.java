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

package sonia.scm.repository.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryTestData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryPermissionsClearCommandTest {

  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private RepositoryTemplateRenderer templateRenderer;

  @InjectMocks
  private RepositoryPermissionsClearCommand command;

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @Nested
  class ForExistingRepository {

    @BeforeEach
    void mockRepository() {
      when(repositoryManager.get(new NamespaceAndName("hitchhiker", "HeartOfGold")))
        .thenReturn(repository);
    }

    @Test
    void shouldClearPermissionsForUser() {
      repository.setPermissions(
        List.of(
          new RepositoryPermission("trillian", List.of("read"), false)
        )
      );

      command.setRepositoryNamespaceAndName("hitchhiker/HeartOfGold");
      command.setName("trillian");

      command.run();

      verify(repositoryManager).modify(argThat(argument -> {
        assertThat(argument.getPermissions()).isEmpty();
        return true;
      }));
    }

    @Test
    void shouldClearPermissionsForGroup() {
      repository.setPermissions(
        List.of(
          new RepositoryPermission("hog", "READ", true)
        )
      );

      command.setRepositoryNamespaceAndName("hitchhiker/HeartOfGold");
      command.setName("hog");
      command.setForGroup(true);

      command.run();

      verify(repositoryManager).modify(argThat(argument -> {
        assertThat(argument.getPermissions()).isEmpty();
        return true;
      }));
    }
  }

  @Test
  void shouldHandleIllegalNamespaceNameParameter() {
    command.setRepositoryNamespaceAndName("illegal name");
    command.setName("trillian");

    command.run();

    verify(templateRenderer).renderInvalidInputError();
  }

  @Test
  void shouldHandleNotExistingRepository() {
    command.setRepositoryNamespaceAndName("no/repository");
    command.setName("trillian");

    command.run();

    verify(templateRenderer).renderNotFoundError();
  }
}
