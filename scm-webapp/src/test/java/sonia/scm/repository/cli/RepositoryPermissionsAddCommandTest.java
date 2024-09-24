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
        command.setRepositoryNamespaceAndName("hitchhiker/HeartOfGold");
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

        command.setRepositoryNamespaceAndName("hitchhiker/HeartOfGold");
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

        command.setRepositoryNamespaceAndName("hitchhiker/HeartOfGold");
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

        command.setRepositoryNamespaceAndName("hitchhiker/HeartOfGold");
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

        command.setRepositoryNamespaceAndName("hitchhiker/HeartOfGold");
        command.setName("trillian");
        command.setVerbs("read");

        command.run();

        verify(repositoryManager, never()).modify(any());
      }
    }

    @Test
    void shouldHandleMissingVerb() {
      command.setRepositoryNamespaceAndName("hitchhiker/HeartOfGold");
      command.setName("trillian");
      command.setVerbs("make-party");

      command.run();

      verify(templateRenderer).renderVerbNotFoundError("make-party");
    }
  }

  @Test
  void shouldHandleIllegalNamespaceNameParameter() {
    command.setRepositoryNamespaceAndName("illegal name");
    command.setName("trillian");
    command.setVerbs("write");

    command.run();

    verify(templateRenderer).renderInvalidInputError();
  }

  @Test
  void shouldHandleNotExistingRepository() {
    command.setRepositoryNamespaceAndName("no/repository");
    command.setName("trillian");
    command.setVerbs("write");

    command.run();

    verify(templateRenderer).renderNotFoundError();
  }
}
