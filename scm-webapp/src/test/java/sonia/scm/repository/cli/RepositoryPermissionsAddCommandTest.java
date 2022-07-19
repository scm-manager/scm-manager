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
import sonia.scm.cli.PermissionDescriptionResolver;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleManager;
import sonia.scm.repository.RepositoryTestData;

import java.util.List;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryPermissionsAddCommandTest {

  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private RepositoryRoleManager roleManager;
  @Mock
  private PermissionDescriptionResolver permissionDescriptionResolver;
  @Mock
  private RepositoryTemplateRenderer templateRenderer;

  @InjectMocks
  private RepositoryPermissionsAddCommand command;

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @Nested
  class ForExistingRepository {

    @BeforeEach
    void mockRepository() {
      when(repositoryManager.get(new NamespaceAndName("hitchhiker", "HeartOfGold")))
        .thenReturn(repository);
    }

    @Nested
    class ForExistingVerbs {

      @BeforeEach
      void mockVerbs() {
        when(permissionDescriptionResolver.getDescription(anyString()))
          .thenAnswer(invocation -> of(invocation.getArgument(0, String.class)));
      }

      @Test
      void shouldSetMultipleVerbsForNewUser() {
        command.setRepositoryName("hitchhiker/HeartOfGold");
        command.setName("trillian");
        command.setVerbs("read", "pull", "push");

        command.run();

        verify(repositoryManager).modify(argThat(argument -> {
          assertThat(argument.getPermissions()).extracting("name", "verbs", "groupPermission")
            .containsExactly(tuple("trillian", Set.of("read", "pull", "push"), false));
          return true;
        }));
      }

      @Test
      void shouldAddNewVerbToExistingVerbsForUser() {
        repository.setPermissions(
          List.of(
            new RepositoryPermission("trillian", List.of("read"), false)
          )
        );

        command.setRepositoryName("hitchhiker/HeartOfGold");
        command.setName("trillian");
        command.setVerbs("write");

        command.run();

        verify(repositoryManager).modify(argThat(argument -> {
          assertThat(argument.getPermissions()).extracting("name", "verbs", "groupPermission")
            .containsExactly(tuple("trillian", Set.of("read", "write"), false));
          return true;
        }));
      }

      @Test
      void shouldAddNewVerbToExistingVerbsForGroup() {
        repository.setPermissions(
          List.of(
            new RepositoryPermission("hog", List.of("read"), true)
          )
        );

        command.setRepositoryName("hitchhiker/HeartOfGold");
        command.setName("hog");
        command.setVerbs("write");
        command.setForGroup(true);

        command.run();

        verify(repositoryManager).modify(argThat(argument -> {
          assertThat(argument.getPermissions()).extracting("name", "verbs", "groupPermission")
            .containsExactly(tuple("hog", Set.of("read", "write"), true));
          return true;
        }));
      }

      @Test
      void shouldAddNewVerbToRoleAndReplaceRoleWithCustomPermissionsForUser() {
        repository.setPermissions(
          List.of(
            new RepositoryPermission("trillian", "READ", false)
          )
        );
        when(roleManager.get("READ"))
          .thenReturn(new RepositoryRole("READ", List.of("read", "pull"), ""));

        command.setRepositoryName("hitchhiker/HeartOfGold");
        command.setName("trillian");
        command.setVerbs("write");

        command.run();

        verify(repositoryManager).modify(argThat(argument -> {
          assertThat(argument.getPermissions()).extracting("name", "verbs", "groupPermission")
            .containsExactly(tuple("trillian", Set.of("pull", "read", "write"), false));
          return true;
        }));
      }

      @Test
      void shouldNotModifyRoleIfNewVerbIsPartOfRole() {
        repository.setPermissions(
          List.of(
            new RepositoryPermission("trillian", "READ", false)
          )
        );
        when(roleManager.get("READ"))
          .thenReturn(new RepositoryRole("READ", List.of("read", "pull"), ""));

        command.setRepositoryName("hitchhiker/HeartOfGold");
        command.setName("trillian");
        command.setVerbs("read");

        command.run();

        verify(repositoryManager, never()).modify(any());
      }
    }

    @Test
    void shouldHandleMissingVerb() {
      command.setRepositoryName("hitchhiker/HeartOfGold");
      command.setName("trillian");
      command.setVerbs("make-party");

      command.run();

      verify(templateRenderer).renderVerbNotFoundError();
    }
  }

  @Test
  void shouldHandleIllegalNamespaceNameParameter() {
    command.setRepositoryName("illegal name");
    command.setName("trillian");
    command.setVerbs("write");

    command.run();

    verify(templateRenderer).renderInvalidInputError();
  }

  @Test
  void shouldHandleNotExistingRepository() {
    command.setRepositoryName("no/repository");
    command.setName("trillian");
    command.setVerbs("write");

    command.run();

    verify(templateRenderer).renderNotFoundError();
  }
}
