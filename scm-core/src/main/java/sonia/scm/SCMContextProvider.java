/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.group.GroupManager;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.security.EncryptionHandler;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.File;

/**
 *
 * @author Sebastian Sdorra
 */
public interface SCMContextProvider extends Closeable
{

  /**
   * Method description
   *
   */
  public void init();

  //~--- get methods ----------------------------------------------------------

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
   * @return
   */
  public EncryptionHandler getEncryptionHandler();

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
   * @return
   */
  public RepositoryManager getRepositoryManager();
}
