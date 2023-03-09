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

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.NotFoundException;
import sonia.scm.ScmConstraintViolationException;
import sonia.scm.security.RepositoryPermissionProvider;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultRepositoryRoleManagerTest {

  private static final String CUSTOM_ROLE_NAME = "customRole";
  private static final String SYSTEM_ROLE_NAME = "systemRole";
  private static final RepositoryRole CUSTOM_ROLE = new RepositoryRole(CUSTOM_ROLE_NAME, singletonList("custom"), "xml");
  private static final RepositoryRole SYSTEM_ROLE = new RepositoryRole(SYSTEM_ROLE_NAME, singletonList("system"), "system");

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  @Mock
  private RepositoryRoleDAO dao;
  @Mock
  private RepositoryPermissionProvider permissionProvider;

  private DefaultRepositoryRoleManager manager;

  @BeforeEach
  void initUser() {
    subjectThreadState.bind();
    doAnswer(invocation -> {
      String permission = invocation.getArguments()[0].toString();
      if (!subject.isPermitted(permission)) {
        throw new UnauthorizedException(permission);
      }
      return null;
    }).when(subject).checkPermission(anyString());
    ThreadContext.bind(subject);
  }

  @BeforeEach
  void initDao() {
    when(dao.getType()).thenReturn("xml");
  }

  @BeforeEach
  void mockExistingRole() {
    when(dao.get(CUSTOM_ROLE_NAME)).thenReturn(CUSTOM_ROLE);
    when(permissionProvider.availableRoles()).thenReturn(asList(CUSTOM_ROLE, SYSTEM_ROLE));
  }

  @BeforeEach
  void initManager() {
    manager = new DefaultRepositoryRoleManager(dao, permissionProvider, emptySet());
  }

  @AfterEach
  void cleanupContext() {
    ThreadContext.unbindSubject();
  }

  @Nested
  class WithAuthorizedUser {

    @BeforeEach
    void authorizeUser() {
      when(subject.isPermitted("repositoryRole:write")).thenReturn(true);
    }

    @Test
    void shouldReturnNull_forNotExistingRole() {
      RepositoryRole role = manager.get("noSuchRole");
      assertThat(role).isNull();
    }

    @Test
    void shouldReturnRole_forExistingRole() {
      RepositoryRole role = manager.get(CUSTOM_ROLE_NAME);
      assertThat(role).isNotNull();
    }

    @Test
    void shouldCreateRole() {
      RepositoryRole role = manager.create(new RepositoryRole("new", singletonList("custom"), null));
      assertThat(role.getType()).isEqualTo("xml");
      verify(dao).add(role);
    }

    @Test
    void shouldNotCreateRole_whenSystemRoleExists() {
      assertThrows(UnauthorizedException.class, () -> manager.create(new RepositoryRole(SYSTEM_ROLE_NAME, singletonList("custom"), null)));
      verify(dao, never()).add(any());
    }

    @Test
    void shouldModifyRole() {
      RepositoryRole role = new RepositoryRole(CUSTOM_ROLE_NAME, singletonList("changed"), "xml");
      manager.modify(role);
      verify(dao).modify(role);
    }

    @Test
    void shouldNotModifyRole_whenRoleDoesNotExists() {
      assertThrows(NotFoundException.class, () -> manager.modify(new RepositoryRole("noSuchRole", singletonList("changed"), null)));
      verify(dao, never()).modify(any());
    }

    @Test
    void shouldNotModifyRole_whenSystemRoleExists() {
      assertThrows(UnauthorizedException.class, () -> manager.modify(new RepositoryRole(SYSTEM_ROLE_NAME, singletonList("changed"), null)));
      verify(dao, never()).modify(any());
    }

    @Test
    void shouldReturnAllRoles() {
      List<RepositoryRole> allRoles = manager.getAll();
      assertThat(allRoles).containsExactly(CUSTOM_ROLE, SYSTEM_ROLE);
    }

    @Test
    void shouldReturnFilteredRoles() {
      Collection<RepositoryRole> allRoles = manager.getAll(role -> CUSTOM_ROLE_NAME.equals(role.getName()), null);
      assertThat(allRoles).containsExactly(CUSTOM_ROLE);
    }

    @Test
    void shouldReturnOrderedFilteredRoles() {
      Collection<RepositoryRole> allRoles =
        manager.getAll(
          role -> true,
          Comparator.comparing(RepositoryRole::getType));
      assertThat(allRoles).containsExactly(SYSTEM_ROLE, CUSTOM_ROLE);
    }

    @Test
    void shouldReturnPaginatedRoles() {
      Collection<RepositoryRole> allRoles =
        manager.getAll(
          Comparator.comparing(RepositoryRole::getType),
          1, 1
        );
      assertThat(allRoles).containsExactly(CUSTOM_ROLE);
    }
  }

  @Nested
  class WithUnauthorizedUser {

    @BeforeEach
    void authorizeUser() {
      when(subject.isPermitted(any(String.class))).thenReturn(false);
    }

    @Test
    void shouldReturnNull_forNotExistingRole() {
      RepositoryRole role = manager.get("noSuchRole");
      assertThat(role).isNull();
    }

    @Test
    void shouldReturnRole_forExistingRole() {
      RepositoryRole role = manager.get(CUSTOM_ROLE_NAME);
      assertThat(role).isNotNull();
    }

    @Test
    void shouldThrowException_forCreate() {
      assertThrows(UnauthorizedException.class, () -> manager.create(new RepositoryRole("new", singletonList("custom"), null)));
      verify(dao, never()).add(any());
    }

    @Test
    void shouldThrowException_forModify() {
      assertThrows(UnauthorizedException.class, () -> manager.modify(new RepositoryRole(CUSTOM_ROLE_NAME, singletonList("custom"), null)));
      verify(dao, never()).modify(any());
    }

    @Test
    void shouldReturnAllRoles() {
      List<RepositoryRole> allRoles = manager.getAll();
      assertThat(allRoles).containsExactly(CUSTOM_ROLE, SYSTEM_ROLE);
    }

    @Test
    void shouldReturnFilteredList() {
      Collection<RepositoryRole> allRoles = manager.getAll(role -> CUSTOM_ROLE_NAME.equals(role.getName()), null);
      assertThat(allRoles).containsExactly(CUSTOM_ROLE);
    }

    @Test
    void shouldReturnPaginatedRoles() {
      Collection<RepositoryRole> allRoles =
        manager.getAll(
          Comparator.comparing(RepositoryRole::getType),
          1, 1
        );
      assertThat(allRoles).containsExactly(CUSTOM_ROLE);
    }
  }
}
