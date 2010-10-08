/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.SCMContextProvider;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractRepositoryManagerDecorator
        implements RepositoryManager
{

  /**
   * Constructs ...
   *
   *
   * @param orginal
   */
  public AbstractRepositoryManagerDecorator(RepositoryManager orginal)
  {
    this.orginal = orginal;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param handler
   */
  @Override
  public void addHandler(RepositoryHandler handler)
  {
    orginal.addHandler(handler);
  }

  /**
   * Method description
   *
   *
   * @param listener
   */
  @Override
  public void addListener(RepositoryListener listener)
  {
    orginal.addListener(listener);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    orginal.close();
  }

  /**
   * Method description
   *
   *
   * @param context
   */
  @Override
  public void init(SCMContextProvider context)
  {
    orginal.init(context);
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
    orginal.addListener(listener);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param type
   *
   * @return
   */
  @Override
  public RepositoryHandler getHandler(String type)
  {
    return orginal.getHandler(type);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<RepositoryType> getTypes()
  {
    return orginal.getTypes();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected RepositoryManager orginal;
}
