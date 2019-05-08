package sonia.scm.repository;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.RepositoryPermissionProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultRepositoryRoleManagerTest {

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  @Mock
  private RepositoryRoleDAO dao;
  @Mock
  private RepositoryPermissionProvider permissionProvider;

  @InjectMocks
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

  @AfterEach
  void cleanupContext() {
    ThreadContext.unbindSubject();
  }

  @Nested
  class WithAuthorizedUser {

    @BeforeEach
    void authorizeUser() {
      when(subject.isPermitted(any(String.class))).thenReturn(true);
    }

    @Test
    void shouldReturnNull_forNotExistingRole() {
      RepositoryRole role = manager.get("noSuchRole");
      assertThat(role).isNull();
    }
  }

  @Nested
  class WithUnauthorizedUser {

    @BeforeEach
    void authorizeUser() {
      when(subject.isPermitted(any(String.class))).thenReturn(false);
    }

    @Test
    void x() {
      assertThrows(UnauthorizedException.class, () -> manager.get("noSuchRole"));
    }
  }
}
