/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sonia.scm;

import sonia.scm.group.GroupManager;
import sonia.scm.repository.RepositoryManager;

/**
 *
 * @author Sebastian Sdorra
 */
public class BasicContextProvider implements SCMContextProvider {

  @Override
  public GroupManager getGroupManager()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public RepositoryManager getRepositoryManager()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
