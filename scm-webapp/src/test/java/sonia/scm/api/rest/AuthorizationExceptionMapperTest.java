package sonia.scm.api.rest;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthorizationExceptionMapperTest {

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  @BeforeEach
  public void init() {
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }

  @AfterEach
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldMapNormalUserToForbidden() {
    when(subject.getPrincipal()).thenReturn("someone");

    assertThat(
      new AuthorizationExceptionMapper().toResponse(new AuthorizationException()).getStatus()
    ).isEqualTo(403);
  }

  @Test
  void shouldMapAnonymousUserToUnauthorized() {
    when(subject.getPrincipal()).thenReturn("_anonymous");

    assertThat(
      new AuthorizationExceptionMapper().toResponse(new AuthorizationException()).getStatus()
    ).isEqualTo(401);
  }
}
