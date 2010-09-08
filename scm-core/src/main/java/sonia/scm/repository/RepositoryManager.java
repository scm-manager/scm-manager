/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.Manager;

/**
 *
 * @author Sebastian Sdorra
 */
public interface RepositoryManager
        extends Manager<Repository, RepositoryException>
{

  /**
   * Method description
   *
   *
   * @return
   */
  public RepositoryType getType();
}
