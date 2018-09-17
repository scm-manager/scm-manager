package sonia.scm.repository;

import sonia.scm.plugin.ExtensionPoint;


/**
 * A pre processor for {@link Modifications} objects. A pre processor is able to
 * modify the object before it is delivered to the user interface.
 *
 * @author Mohamed Karray
 * @since 2.0
 */
@ExtensionPoint
public interface ModificationsPreProcessor extends PreProcessor<Modifications> {

  /**
   * Process the given modifications.
   *
   * @param modifications modifications to process
   */
  @Override
  void process(Modifications modifications);
}
