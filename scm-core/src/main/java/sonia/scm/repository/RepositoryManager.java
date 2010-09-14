/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.Handler;
import sonia.scm.ListenerSupport;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public interface RepositoryManager
        extends Handler<Repository, RepositoryException>,
                ListenerSupport<RepositoryListener>
{

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<RepositoryType> getTypes();
}
