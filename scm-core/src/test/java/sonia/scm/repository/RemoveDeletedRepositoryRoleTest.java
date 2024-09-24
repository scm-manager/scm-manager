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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import sonia.scm.HandlerEventType;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static sonia.scm.HandlerEventType.DELETE;

@ExtendWith(MockitoExtension.class)
class RemoveDeletedRepositoryRoleTest {

  static final Repository REPOSITORY = createRepositoryWithRoles("with", "deleted", "kept");

  @Mock
  RepositoryManager manager;
  @Captor
  ArgumentCaptor<Repository> modifyCaptor;

  private RemoveDeletedRepositoryRole removeDeletedRepositoryRole;

  @BeforeEach
  void init() {
    removeDeletedRepositoryRole = new RemoveDeletedRepositoryRole(manager);
    doNothing().when(manager).modify(modifyCaptor.capture());
  }

  @Test
  void shouldRemoveDeletedPermission() {
    when(manager.getAll()).thenReturn(Collections.singletonList(REPOSITORY));

    removeDeletedRepositoryRole.handle(new RepositoryRoleEvent(DELETE, createRole("deleted")));

    verify(manager).modify(any());
    Assertions.assertThat(modifyCaptor.getValue().getPermissions())
      .containsExactly(createPermission("kept"));
  }

  @Test
  @MockitoSettings(strictness = LENIENT)
  void shouldDoNothingForEventsWithUnusedRole() {
    when(manager.getAll()).thenReturn(Collections.singletonList(REPOSITORY));

    removeDeletedRepositoryRole.handle(new RepositoryRoleEvent(DELETE, createRole("unused")));

    verify(manager, never()).modify(any());
  }

  @Test
  @MockitoSettings(strictness = LENIENT)
  void shouldDoNothingForEventsOtherThanDelete() {
    when(manager.getAll()).thenReturn(Collections.singletonList(REPOSITORY));

    Arrays.stream(HandlerEventType.values())
      .filter(type -> type != DELETE)
      .forEach(
        type -> removeDeletedRepositoryRole.handle(new RepositoryRoleEvent(type, createRole("deleted")))
      );

    verify(manager, never()).modify(any());
  }

  private RepositoryRole createRole(String name) {
    return new RepositoryRole(name, Collections.singleton("x"), "x");
  }

  static Repository createRepositoryWithRoles(String name, String... roles) {
    Repository repository = new Repository("x", "git", "space", name);
    Arrays.stream(roles).forEach(role -> repository.addPermission(createPermission(role)));
    return repository;
  }

  private static RepositoryPermission createPermission(String role) {
    return new RepositoryPermission("user", role, false);
  }
}
