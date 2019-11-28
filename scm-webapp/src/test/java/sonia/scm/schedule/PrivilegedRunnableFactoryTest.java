package sonia.scm.schedule;

import com.google.inject.util.Providers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class PrivilegedRunnableFactoryTest {

  @Mock
  private AdministrationContext administrationContext;

  @InjectMocks
  private PrivilegedRunnableFactory runnableFactory;

  @Test
  void shouldRunAsPrivilegedAction() {
    doAnswer((ic) -> {
      PrivilegedAction action = ic.getArgument(0);
      action.run();
      return null;
    }).when(administrationContext).runAsAdmin(any(PrivilegedAction.class));

    RemindingRunnable runnable = new RemindingRunnable();

    Runnable action = runnableFactory.create(Providers.of(runnable));
    assertThat(action).isNotExactlyInstanceOf(RemindingRunnable.class);

    assertThat(runnable.run).isFalse();
    action.run();
    assertThat(runnable.run).isTrue();
  }

  private static class RemindingRunnable implements Runnable {

    private boolean run = false;

    @Override
    public void run() {
      run = true;
    }
  }

}
