package sonia.scm.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.HandlerEventType;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AnonymousUserDeletionEventHandlerTest {

  private ScmConfiguration scmConfiguration;

  private AnonymousUserDeletionEventHandler hook;

  @BeforeEach
  void initConfig() {
    scmConfiguration = new ScmConfiguration();
  }

  @Test
  void shouldThrowAnonymousUserDeletionExceptionIfAnonymousAccessIsEnabled() {
    scmConfiguration.setAnonymousAccessEnabled(true);

    hook = new AnonymousUserDeletionEventHandler(scmConfiguration);
    UserEvent deletionEvent = new UserEvent(HandlerEventType.BEFORE_DELETE, SCMContext.ANONYMOUS);

    assertThrows(AnonymousUserDeletionException.class, () -> hook.onEvent(deletionEvent));
  }

  @Test
  void shouldNotThrowAnonymousUserDeletionException() {
    scmConfiguration.setAnonymousAccessEnabled(false);

    hook = new AnonymousUserDeletionEventHandler(scmConfiguration);
    UserEvent deletionEvent = new UserEvent(HandlerEventType.BEFORE_DELETE,  SCMContext.ANONYMOUS);

    hook.onEvent(deletionEvent);
  }
}
