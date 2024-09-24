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
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleManager;
import sonia.scm.repository.RepositoryTestData;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryPermissionsRemoveCommandTest {

  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private RepositoryRoleManager roleManager;
  @Mock
  private RepositoryTemplateRenderer templateRenderer;

  @InjectMocks
  private RepositoryPermissionsRemoveCommand command;

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @Nested
  class ForExistingRepository {

    @BeforeEach
    void mockRepository() {
      when(repositoryManager.get(new NamespaceAndName("hitchhiker", "HeartOfGold")))
        .thenReturn(repository);
    }

    @Test
    void shouldRemoveMultipleVerbsFromExistingVerbsForUser() {
      repository.setPermissions(
        List.of(
          new RepositoryPermission("dent", List.of("read", "write", "push", "pull"), false)
        )
      );

      command.setRepositoryNamespaceAndName("hitchhiker/HeartOfGold");
      command.setName("dent");
      command.setVerbs("write", "push", "pull");

      command.run();

      verify(repositoryManager).modify(argThat(argument -> {
        assertThat(argument.getPermissions()).extracting("name", "verbs", "groupPermission")
          .containsExactly(tuple("dent", Set.of("read"), false));
        return true;
      }));
    }

    @Test
    void shouldRemoveNewVerbFromExistingVerbsForGroup() {
      repository.setPermissions(
        List.of(
          new RepositoryPermission("hog", List.of("read", "write"), true)
        )
      );

      command.setRepositoryNamespaceAndName("hitchhiker/HeartOfGold");
      command.setName("hog");
      command.setVerbs("write");
      command.setForGroup(true);

      command.run();

      verify(repositoryManager).modify(argThat(argument -> {
        assertThat(argument.getPermissions()).extracting("name", "verbs", "groupPermission")
          .containsExactly(tuple("hog", Set.of("read"), true));
        return true;
      }));
    }

    @Test
    void shouldRemoveNewVerbToRoleAndReplaceRoleWithCustomPermissionsForUser() {
      repository.setPermissions(
        List.of(
          new RepositoryPermission("dent", "READ", false)
        )
      );
      when(roleManager.get("READ"))
        .thenReturn(new RepositoryRole("READ", List.of("read", "pull"), ""));

      command.setRepositoryNamespaceAndName("hitchhiker/HeartOfGold");
      command.setName("dent");
      command.setVerbs("pull");

      command.run();

      verify(repositoryManager).modify(argThat(argument -> {
        assertThat(argument.getPermissions()).extracting("name", "verbs", "groupPermission")
          .containsExactly(tuple("dent", Set.of("read"), false));
        return true;
      }));
    }

    @Test
    void shouldNotModifyRepositoryIfVerbsAreNotSet() {
      repository.setPermissions(
        List.of(
          new RepositoryPermission("dent", List.of("read", "write"), false)
        )
      );

      command.setRepositoryNamespaceAndName("hitchhiker/HeartOfGold");
      command.setName("dent");
      command.setVerbs("push", "pull");

      command.run();

      verify(repositoryManager, never()).modify(any());
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
