package sonia.scm.web.security;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultAdministrationContextTest {

  private DefaultAdministrationContext context;

  @Mock
  private Subject subject;

  @BeforeEach
  void create() {
    Injector injector = Guice.createInjector();
    SecurityManager securityManager = new DefaultSecurityManager();

    context = new DefaultAdministrationContext(injector, securityManager);
  }

  @Test
  void shouldBindSubject() {
    context.runAsAdmin(() -> {
      Subject adminSubject = SecurityUtils.getSubject();
      assertThat(adminSubject.getPrincipal()).isEqualTo("scmsystem");
    });
  }

  @Test
  void shouldBindSubjectEvenIfAlreadyBound() {
    ThreadContext.bind(subject);
    try {

      context.runAsAdmin(() -> {
        Subject adminSubject = SecurityUtils.getSubject();
        assertThat(adminSubject.getPrincipal()).isEqualTo("scmsystem");
      });

    } finally {
      ThreadContext.unbindSubject();
    }
  }

  @Test
  void shouldRestoreCurrentSubject() {
    when(subject.getPrincipal()).thenReturn("tricia");
    ThreadContext.bind(subject);
    try {
      context.runAsAdmin(() -> {});
      Subject currentSubject = SecurityUtils.getSubject();
      assertThat(currentSubject.getPrincipal()).isEqualTo("tricia");
    } finally {
      ThreadContext.unbindSubject();
    }
  }

}
