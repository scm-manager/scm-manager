/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractRepositoryManager implements RepositoryManager
{

  /**
   * Method description
   *
   *
   * @param listener
   */
  @Override
  public void addListener(RepositoryListener listener)
  {
    listenerSet.add(listener);
  }

  /**
   * Method description
   *
   *
   * @param listener
   */
  @Override
  public void removeListener(RepositoryListener listener)
  {
    listenerSet.remove(listener);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param event
   */
  protected void fireEvent(Repository repository, RepositoryEvent event)
  {
    for (RepositoryListener listener : listenerSet)
    {
      listener.onEvent(repository, event);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<RepositoryListener> listenerSet =
    new HashSet<RepositoryListener>();
}
