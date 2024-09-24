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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.PermissionDescriptionResolver;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleManager;
import sonia.scm.security.RepositoryPermissionProvider;

import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryPermissionsAvailableCommandTest {

  @Mock
  private RepositoryTemplateRenderer templateRenderer;
  @Mock
  private RepositoryPermissionProvider repositoryPermissionProvider;
  @Mock
  private PermissionDescriptionResolver permissionDescriptionResolver;
  @Mock
  private RepositoryRoleManager repositoryRoleManager;

  @InjectMocks
  private RepositoryPermissionsAvailableCommand command;

  @Captor
  private ArgumentCaptor<Collection<VerbBean>> verbsCaptor;
  @Captor
  private ArgumentCaptor<Collection<RoleBean>> rolesCaptor;

  @Test
  void shouldListVerbs() {
    doNothing().when(templateRenderer).render(any(), verbsCaptor.capture());
    when(repositoryPermissionProvider.availableVerbs())
      .thenReturn(List.of("read", "write"));
    when(permissionDescriptionResolver.getDescription("read"))
      .thenReturn(of("read repository"));
    when(permissionDescriptionResolver.getDescription("write"))
      .thenReturn(of("write repository"));

    command.run();

    Collection<VerbBean> capturedVerbs = verbsCaptor.getValue();

    assertThat(capturedVerbs).
      extracting("verb")
      .containsExactly("read", "write");
    assertThat(capturedVerbs).
      extracting("description")
      .containsExactly("read repository", "write repository");
  }

  @Test
  void shouldHandleMissingDescription() {
    doNothing().when(templateRenderer).render(any(), verbsCaptor.capture());
    when(repositoryPermissionProvider.availableVerbs())
      .thenReturn(List.of("unknown"));
    when(permissionDescriptionResolver.getDescription("unknown"))
      .thenReturn(empty());

    command.run();

    Collection<VerbBean> capturedVerbs = verbsCaptor.getValue();

    assertThat(capturedVerbs).
      extracting("verb")
      .containsExactly("unknown");
    assertThat(capturedVerbs).
      extracting("description")
      .containsExactly("unknown");
  }

  @Test
  void shouldRenderRoles() {
    doNothing().when(templateRenderer).render(rolesCaptor.capture(), any());
    when(repositoryRoleManager.getAll())
      .thenReturn(List.of(new RepositoryRole("READ", List.of("read", "pull"), null)));

    command.run();

    Collection<RoleBean> capturedRoles = rolesCaptor.getValue();

    assertThat(capturedRoles)
      .extracting("name")
      .containsExactly("READ");
    assertThat(capturedRoles)
      .extracting("verbs")
      .map(c -> ((Collection) c).stream().collect(toList())) // to satisfy equal in the comparison, we have to use this form
      .containsExactly(List.of("read", "pull"));
  }

  @Test
  void shouldRenderRolesOnlyWithFlag() {
    command.setRoles(true);

    command.run();

    verify(templateRenderer).renderRoles(emptyList());
    verify(templateRenderer, never()).renderVerbs(any());
  }

  @Test
  void shouldRenderVerbsOnlyWithFlag() {
    command.setVerbs(true);

    command.run();

    verify(templateRenderer).renderVerbs(emptyList());
    verify(templateRenderer, never()).renderRoles(any());
  }
}
