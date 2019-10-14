package sonia.scm.config;

import com.github.legman.Subscribe;
import com.google.inject.Inject;
import sonia.scm.EagerSingleton;
import sonia.scm.SCMContext;
import sonia.scm.plugin.Extension;
import sonia.scm.user.UserManager;

@Extension
@EagerSingleton
public class ScmConfigurationChangedListener {

  private UserManager userManager;

  @Inject
  public ScmConfigurationChangedListener(UserManager userManager) {
    this.userManager = userManager;
  }

  @Subscribe
  public void handleEvent(ScmConfigurationChangedEvent event) {
    if (event.getConfiguration().isAnonymousAccessEnabled() && !userManager.contains(SCMContext.USER_ANONYMOUS)) {
      userManager.create(SCMContext.ANONYMOUS);
    }
  }
}


