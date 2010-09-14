/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.SCMContextProvider;
import sonia.scm.util.ServiceUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class BasicRepositoryManager extends AbstractRepositoryManager
{

  /**
   * Constructs ...
   *
   */
  public BasicRepositoryManager()
  {
    handlerMap = new HashMap<String, RepositoryHandler>();
    types = new ArrayList<RepositoryType>();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    for (RepositoryHandler manager : handlerMap.values())
    {
      manager.close();
    }
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void create(Repository repository)
          throws RepositoryException, IOException
  {
    getHandler(repository).create(repository);
    fireEvent(repository, RepositoryEvent.CREATE);
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void delete(Repository repository)
          throws RepositoryException, IOException
  {
    getHandler(repository).delete(repository);
    fireEvent(repository, RepositoryEvent.DELETE);
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
    List<RepositoryHandler> handlerList =
      ServiceUtil.getServices(RepositoryHandler.class);

    if (Util.isNotEmpty(handlerList))
    {
      for (RepositoryHandler handler : handlerList)
      {
        RepositoryType type = handler.getType();

        types.add(type);
        handlerMap.put(type.getName(), handler);
        handler.init(context);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void modify(Repository repository)
          throws RepositoryException, IOException
  {
    getHandler(repository).modify(repository);
    fireEvent(repository, RepositoryEvent.MODIFY);
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void refresh(Repository repository)
          throws RepositoryException, IOException
  {
    getHandler(repository).refresh(repository);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public Repository get(String id)
  {
    Repository repository = null;

    for (RepositoryHandler handler : handlerMap.values())
    {
      repository = handler.get(id);

      if (repository != null)
      {
        break;
      }
    }

    return repository;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Repository> getAll()
  {
    Set<Repository> repositories = new HashSet<Repository>();

    for (RepositoryHandler handler : handlerMap.values())
    {
      Collection<Repository> handlerRepositories = handler.getAll();

      if (handlerRepositories != null)
      {
        repositories.addAll(handlerRepositories);
      }
    }

    return repositories;
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
    return types;
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   *
   * @throws RepositoryHandlerNotFoundException
   */
  private RepositoryHandler getHandler(Repository repository)
          throws RepositoryHandlerNotFoundException
  {
    String type = repository.getType();
    RepositoryHandler handler = handlerMap.get(type);

    if (handler == null)
    {
      throw new RepositoryHandlerNotFoundException(
          "could not find handler for ".concat(type));
    }

    return handler;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Map<String, RepositoryHandler> handlerMap;

  /** Field description */
  private List<RepositoryType> types;
}
