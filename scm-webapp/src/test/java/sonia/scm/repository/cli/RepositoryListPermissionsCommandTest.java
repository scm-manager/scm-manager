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
import sonia.scm.cli.Table;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleManager;
import sonia.scm.repository.RepositoryTestData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryListPermissionsCommandTest {

  @Mock
  private RepositoryTemplateRenderer templateRenderer;
  @Mock
  private CommandValidator validator;
  @Mock
  private RepositoryManager manager;
  @Mock
  private RepositoryRoleManager roleManager;

  @InjectMocks
  private RepositoryListPermissionsCommand command;

  @Test
  void shouldPrintNotFoundErrorForUnknownRepository() {
    command.setRepository("hg2g/hog");

    command.run();

    verify(templateRenderer).renderNotFoundError();
  }

  @Nested
  class ForExistingRepository {

    @Captor
    private ArgumentCaptor<Map<String, Object>> mapCaptor;
    @Captor
    private ArgumentCaptor<String> templateCaptor;
    @Mock
    private Table table;
    @Captor
    private ArgumentCaptor<String> tableCaptor;

    private final Repository repository = RepositoryTestData.createHeartOfGold();

    @BeforeEach
    void setUpTableMock() {
      when(templateRenderer.createTable()).thenReturn(table);
      doNothing().when(templateRenderer)
        .renderToStdout(templateCaptor.capture(), mapCaptor.capture());
      lenient().doNothing().when(table)
        .addRow(tableCaptor.capture());
    }

    @BeforeEach
    void mockRepository() {
      when(manager.get(new NamespaceAndName("hitchhiker", "HeartOfGold")))
        .thenReturn(repository);
      command.setRepository("hitchhiker/HeartOfGold");
    }

    @Test
    void shouldRenderEmptyTableWithoutPermissions() {
      command.run();

      Map<String, Object> map = mapCaptor.getValue();
      assertThat(map).hasSize(2);
      assertThat(map.get("rows")).isSameAs(table);
      assertThat(map.get("permissions")).matches(o -> {
        assertThat(o).isInstanceOf(Collection.class)
          .extracting("empty")
          .isEqualTo(true);
        return true;
      });
    }

    @Test
    void shouldListUserPermissionWithVerbs() {
      RepositoryPermission permission = new RepositoryPermission("trillian", List.of("read", "write"), false);
      repository.setPermissions(List.of(permission));

      command.run();

      Map<String, Object> map = mapCaptor.getValue();
      assertThat(map).hasSize(2);
      assertThat(map.get("rows")).isSameAs(table);
      assertThat(map.get("permissions")).matches(o -> {
        assertThat((Collection) o)
          .hasSize(1)
          .first()
          .isEqualTo(permission);
        return true;
      });
      assertThat(tableCaptor.getAllValues())
        .containsExactly("", "trillian", null, "read, write");
    }

    @Test
    void shouldListUserPermissionWithRole() {
      RepositoryPermission permission = new RepositoryPermission("trillian", "READ", false);
      repository.setPermissions(List.of(permission));
      when(roleManager.get("READ")).thenReturn(new RepositoryRole("READ", List.of("read", "pull"), ""));

      command.run();

      Map<String, Object> map = mapCaptor.getValue();
      assertThat(map).hasSize(2);
      assertThat(map.get("rows")).isSameAs(table);
      assertThat(map.get("permissions")).matches(o -> {
        assertThat((Collection) o)
          .hasSize(1)
          .first()
          .isEqualTo(permission);
        return true;
      });
      assertThat(tableCaptor.getAllValues())
        .containsExactly("", "trillian", "READ", "read, pull");
    }
  }
}
