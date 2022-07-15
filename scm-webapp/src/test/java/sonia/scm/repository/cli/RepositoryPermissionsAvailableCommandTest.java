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
