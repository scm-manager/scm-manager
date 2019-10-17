package sonia.scm.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.user.UserManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScmConfigurationChangedListenerTest {

  @Mock
  UserManager userManager;

  ScmConfiguration scmConfiguration = new ScmConfiguration();

  @InjectMocks
  ScmConfigurationChangedListener listener = new ScmConfigurationChangedListener(userManager);

  @Test
  void shouldCreateAnonymousUserIfAnoymousAccessEnabled() {
    when(userManager.contains(any())).thenReturn(false);

    ScmConfiguration changes = new ScmConfiguration();
    changes.setAnonymousAccessEnabled(true);
    scmConfiguration.load(changes);

    listener.handleEvent(new ScmConfigurationChangedEvent(scmConfiguration));
    verify(userManager).create(any());
  }

  @Test
  void shouldNotCreateAnonymousUserIfAlreadyExists() {
    when(userManager.contains(any())).thenReturn(true);

    ScmConfiguration changes = new ScmConfiguration();
    changes.setAnonymousAccessEnabled(true);
    scmConfiguration.load(changes);

    listener.handleEvent(new ScmConfigurationChangedEvent(scmConfiguration));
    verify(userManager, never()).create(any());
  }

  @Test
  void shouldNotCreateAnonymousUserIfAnonymousAccessDisabled() {
    ScmConfiguration changes = new ScmConfiguration();
    changes.setAnonymousAccessEnabled(false);
    scmConfiguration.load(changes);

    listener.handleEvent(new ScmConfigurationChangedEvent(scmConfiguration));
    verify(userManager, never()).create(any());
  }

}
