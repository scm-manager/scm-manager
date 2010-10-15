/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.ConfigChangedListener;
import sonia.scm.Handler;
import sonia.scm.ListenerSupport;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <C>
 */
public interface RepositoryHandler
        extends Handler<Repository, RepositoryException>,
                ListenerSupport<ConfigChangedListener>
{

  /**
   * Method description
   *
   *
   * @return
   */
  public RepositoryType getType();

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isConfigured();
}
