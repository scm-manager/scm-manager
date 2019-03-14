package sonia.scm.repository;

import com.google.common.annotations.VisibleForTesting;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Year;

@Extension
public class CurrentYearNamespaceStrategy implements NamespaceStrategy {

  private final Clock clock;

  @Inject
  public CurrentYearNamespaceStrategy() {
    this(Clock.systemDefaultZone());
  }

  @VisibleForTesting
  CurrentYearNamespaceStrategy(Clock clock) {
    this.clock = clock;
  }

  @Override
  public String createNamespace(Repository repository) {
    return String.valueOf(Year.now(clock).getValue());
  }
}
