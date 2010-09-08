/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.group.GroupManager;
import sonia.scm.repository.RepositoryManager;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

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
  public File getBaseDirectory();

  /**
   * Method description
   *
   *
   *
   * @param type
   * @return
   */
  public GroupManager getGroupManager(String type);

  /**
   * Method description
   *
   *
   *
   * @param type
   * @return
   */
  public RepositoryManager getRepositoryManager(String type);
}
