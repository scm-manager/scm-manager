package sonia.scm.user;

import com.github.legman.Subscribe;
import sonia.scm.ContextEntry;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;

@EagerSingleton
@Extension
public class AnonymousUserDeletionEventHandler {

  private ScmConfiguration scmConfiguration;

  @Inject
  public AnonymousUserDeletionEventHandler(ScmConfiguration scmConfiguration) {
    this.scmConfiguration = scmConfiguration;
  }

  @Subscribe(async = false)
  public void onEvent(UserEvent event) {
    if (isAnonymousUserDeletionNotAllowed(event)) {
      throw new AnonymousUserDeletionException(ContextEntry.ContextBuilder.entity(User.class, event.getItem().getId()));
    }
  }

  private boolean isAnonymousUserDeletionNotAllowed(UserEvent event) {
    return event.getEventType() == HandlerEventType.BEFORE_DELETE
      && event.getItem().getName().equals(SCMContext.USER_ANONYMOUS)
      && scmConfiguration.isAnonymousAccessEnabled();
  }
}
