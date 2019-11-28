package sonia.scm.repository.api;

import sonia.scm.FeatureNotSupportedException;
import sonia.scm.repository.Feature;
import sonia.scm.repository.spi.DiffCommandRequest;

import java.util.Set;

abstract class AbstractDiffCommandBuilder <T extends AbstractDiffCommandBuilder> {


  /** request for the diff command implementation */
  final DiffCommandRequest request = new DiffCommandRequest();

  private final Set<Feature> supportedFeatures;

  AbstractDiffCommandBuilder(Set<Feature> supportedFeatures) {
    this.supportedFeatures = supportedFeatures;
  }

  /**
   * Compute the incoming changes of the branch set with {@link #setRevision(String)} in respect to the changeset given
   * here. In other words: What changes would be new to the ancestor changeset given here when the branch would
   * be merged into it. Requires feature {@link sonia.scm.repository.Feature#INCOMING_REVISION}!
   *
   * @return {@code this}
   */
  public T setAncestorChangeset(String revision)
  {
    if (!supportedFeatures.contains(Feature.INCOMING_REVISION)) {
      throw new FeatureNotSupportedException(Feature.INCOMING_REVISION.name());
    }
    request.setAncestorChangeset(revision);

    return self();
  }

  /**
   * Show the difference only for the given path.
   *
   *
   * @param path path for difference
   *
   * @return {@code this}
   */
  public T setPath(String path)
  {
    request.setPath(path);
    return self();
  }

  /**
   * Show the difference only for the given revision or (using {@link #setAncestorChangeset(String)}) between this
   * and another revision.
   *
   *
   * @param revision revision for difference
   *
   * @return {@code this}
   */
  public T setRevision(String revision)
  {
    request.setRevision(revision);
    return self();
  }

  abstract T self();
}
