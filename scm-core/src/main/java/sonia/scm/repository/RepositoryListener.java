/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

/**
 *
 * @author Sebastian Sdorra
 */
public interface RepositoryListener
{

  /**
   * Method description
   *
   *
   * @param repository
   * @param event
   */
  public void onEvent(Repository repository, RepositoryEvent event);
}
