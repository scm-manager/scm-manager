/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.Handler;

/**
 *
 * @author Sebastian Sdorra
 */
public interface RepositoryHandler
        extends Handler<Repository, RepositoryException>
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
