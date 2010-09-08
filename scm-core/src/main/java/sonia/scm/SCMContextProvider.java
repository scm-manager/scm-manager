/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.group.GroupManager;
import sonia.scm.repository.RepositoryManager;

/**
 *
 * @author Sebastian Sdorra
 */
public interface SCMContextProvider
{

  /**
   * Method description
   *
   *
   * @return
   */
  public GroupManager getGroupManager();

  /**
   * Method description
   *
   *
   * @return
   */
  public RepositoryManager getRepositoryManager();
}
