package sonia.scm.repository;

import sonia.scm.plugin.ExtensionPoint;


/**
 * This factory create a {@link ModificationsPreProcessor}
 *
 * @author Mohamed Karray
 * @since 2.0
 */
@ExtensionPoint
public interface ModificationsPreProcessorFactory extends PreProcessorFactory<Modifications> {

  /**
   * Create a new {@link ModificationsPreProcessor} for the given repository.
   *
   *
   * @param repository repository
   *
   * @return {@link ModificationsPreProcessor} for the given repository
   */
  @Override
  ModificationsPreProcessor createPreProcessor(Repository repository);

}
