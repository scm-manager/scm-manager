package sonia.scm.lifecycle;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.IntConsumer;

/**
 * {@link RestartStrategy} which tears down the scm-manager context and
 * then exists the java process with {@link System#exit(int)}.
 * <p>
 * This is useful if an external mechanism is able to restart the process after it has exited.
 */
class ExitRestartStrategy implements RestartStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(ExitRestartStrategy.class);

  static final String NAME = "exit";

  static final String PROPERTY_EXIT_CODE = "sonia.scm.restart.exit-code";

  private IntConsumer exiter = System::exit;

  ExitRestartStrategy() {
  }

  @VisibleForTesting
  void setExiter(IntConsumer exiter) {
    this.exiter = exiter;
  }

  @Override
  public void restart(InjectionContext context) {
    int exitCode = determineExitCode();

    LOG.warn("destroy injection context");
    context.destroy();

    LOG.warn("exit scm-manager with exit code {}", exitCode);
    exiter.accept(exitCode);
  }

  private int determineExitCode() {
    String exitCodeAsString = System.getProperty(PROPERTY_EXIT_CODE, "0");
    try {
      return Integer.parseInt(exitCodeAsString);
    } catch (NumberFormatException ex) {
      throw new RestartNotSupportedException("invalid exit code " + exitCodeAsString, ex);
    }
  }
}
