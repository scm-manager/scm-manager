package sonia.scm.repository.api;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Feature;
import sonia.scm.repository.spi.DiffResultCommand;

import java.io.IOException;
import java.util.Set;

public class DiffResultCommandBuilder extends AbstractDiffCommandBuilder<DiffResultCommandBuilder> {

  private static final Logger LOG = LoggerFactory.getLogger(DiffResultCommandBuilder.class);

  private final DiffResultCommand diffResultCommand;

  DiffResultCommandBuilder(DiffResultCommand diffResultCommand, Set<Feature> supportedFeatures) {
    super(supportedFeatures);
    this.diffResultCommand = diffResultCommand;
  }

  /**
   * Returns the content of the difference as parsed objects.
   *
   * @return content of the difference
   */
  public DiffResult getDiffResult() throws IOException {
    Preconditions.checkArgument(request.isValid(),
      "path and/or revision is required");

    LOG.debug("create diff result for {}", request);

    return diffResultCommand.getDiffResult(request);
  }

  @Override
  DiffResultCommandBuilder self() {
    return this;
  }
}
