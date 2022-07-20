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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.CommandValidator;
import sonia.scm.cli.PermissionDescriptionResolver;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleManager;
import sonia.scm.repository.RepositoryTestData;

import java.util.Collection;
import java.util.List;

import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryPermissionsListCommandTest {

  @Mock
  private RepositoryTemplateRenderer templateRenderer;
  @Mock
  private CommandValidator validator;
  @Mock
  private RepositoryManager manager;
  @Mock
  private RepositoryRoleManager roleManager;
  @Mock
  private PermissionDescriptionResolver permissionDescriptionResolver;

  @InjectMocks
  private RepositoryPermissionsListCommand command;

  @Test
  void shouldPrintNotFoundErrorForUnknownRepository() {
    command.setRepository("hg2g/hog");

    command.run();

    verify(templateRenderer).renderNotFoundError();
  }

  @Nested
  class ForExistingRepository {

    @Captor
    private ArgumentCaptor<Collection<RepositoryPermissionBean>> permissionsCaptor;

    private final Repository repository = RepositoryTestData.createHeartOfGold();

    @BeforeEach
    void mockRepository() {
      when(manager.get(new NamespaceAndName("hitchhiker", "HeartOfGold")))
        .thenReturn(repository);
      command.setRepository("hitchhiker/HeartOfGold");
    }

    @Nested
    class WithoutVerboseFlag {

      @BeforeEach
      void setUpRenderer() {
        doNothing().when(templateRenderer).render(permissionsCaptor.capture());
      }

      @Test
      void shouldRenderEmptyTableWithoutPermissions() {
        command.run();

        Collection<RepositoryPermissionBean> beans = permissionsCaptor.getValue();
        assertThat(beans).isEmpty();
      }

      @Test
      void shouldListCustomUserPermission() {
        RepositoryPermission permission = new RepositoryPermission("trillian", List.of("read", "write"), false);
        repository.setPermissions(List.of(permission));
        when(permissionDescriptionResolver.getDescription("read"))
          .thenReturn(of("read repository"));
        when(permissionDescriptionResolver.getDescription("write"))
          .thenReturn(of("write repository"));

        command.run();

        Collection<RepositoryPermissionBean> beans = permissionsCaptor.getValue();
        assertThat(beans).extracting("groupPermission", "name", "role")
          .containsExactly(tuple(false, "trillian", "CUSTOM"));
      }
    }

    @Nested
    class WithVerboseFlag {

      @BeforeEach
      void setUpRenderer() {
        doNothing().when(templateRenderer).renderVerbose(permissionsCaptor.capture());
      }

      @BeforeEach
      void setVerbose() {
        command.setVerbose(true);
      }

      @Test
      void shouldListUserPermissionWithVerbs() {
        RepositoryPermission permission = new RepositoryPermission("trillian", List.of("read", "write"), false);
        repository.setPermissions(List.of(permission));
        when(permissionDescriptionResolver.getDescription("read"))
          .thenReturn(of("read repository"));
        when(permissionDescriptionResolver.getDescription("write"))
          .thenReturn(of("write repository"));

        command.run();

        Collection<RepositoryPermissionBean> beans = permissionsCaptor.getValue();
        assertThat(beans).extracting("groupPermission", "name", "role", "verbs")
          .containsExactly(tuple(false, "trillian", "CUSTOM", List.of("read repository", "write repository")));
      }

      @Test
      void shouldListUserPermissionWithRole() {
        RepositoryPermission permission = new RepositoryPermission("trillian", "READ", false);
        repository.setPermissions(List.of(permission));
        when(roleManager.get("READ"))
          .thenReturn(new RepositoryRole("READ", List.of("read", "pull"), ""));
        when(permissionDescriptionResolver.getDescription("read"))
          .thenReturn(of("read repository"));
        when(permissionDescriptionResolver.getDescription("pull"))
          .thenReturn(of("clone/checkout repository"));

        command.run();

        Collection<RepositoryPermissionBean> beans = permissionsCaptor.getValue();
        assertThat(beans).extracting("groupPermission", "name", "verbs")
          .containsExactly(tuple(false, "trillian", List.of("read repository", "clone/checkout repository")));
      }

      @Test
      void shouldListUserPermissionWithVerbsAsKeys() {
        RepositoryPermission permission = new RepositoryPermission("trillian", List.of("read", "write"), false);
        repository.setPermissions(List.of(permission));

        command.setKeys(true);
        command.run();

        Collection<RepositoryPermissionBean> beans = permissionsCaptor.getValue();
        assertThat(beans).extracting("groupPermission", "name", "role")
          .containsExactly(tuple(false, "trillian", "CUSTOM"));
        assertThat(beans).extracting("verbs")
          .map(c -> ((Collection) c).stream().collect(toList())) // to satisfy equal in the comparison, we have to use this form
          .containsExactly(List.of("read", "write"));
      }
    }
  }
}
