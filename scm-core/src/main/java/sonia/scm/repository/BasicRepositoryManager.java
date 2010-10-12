/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.ConfigurationException;
import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.IOUtil;

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
@Singleton
public class BasicRepositoryManager extends AbstractRepositoryManager
{

  /**
   * Constructs ...
   *
   *
   * @param handlerSet
   */
  @Inject
  public BasicRepositoryManager(Set<RepositoryHandler> handlerSet)
  {
    handlerMap = new HashMap<String, RepositoryHandler>();
    types = new ArrayList<RepositoryType>();

    for (RepositoryHandler handler : handlerSet)
    {
      addHandler(handler);
    }
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
    AssertUtil.assertIsNotNull(handler);

    RepositoryType type = handler.getType();

    AssertUtil.assertIsNotNull(type);

    if (handlerMap.containsKey(type.getName()))
    {
      throw new ConfigurationException(
          type.getName().concat("allready registered"));
    }

    handlerMap.put(type.getName(), handler);
    handler.init(SCMContext.getContext());
    types.add(type);
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
    for (RepositoryHandler handler : handlerMap.values())
    {
      IOUtil.close(handler);
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
  public void init(SCMContextProvider context) {}

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
      if (handler.isConfigured())
      {
        repository = handler.get(id);

        if (repository != null)
        {
          break;
        }
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
      if (handler.isConfigured())
      {
        Collection<Repository> handlerRepositories = handler.getAll();

        if (handlerRepositories != null)
        {
          repositories.addAll(handlerRepositories);
        }
      }
    }

    return repositories;
  }

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
    return handlerMap.get(type);
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
   *
   * @throws RepositoryException
   */
  private RepositoryHandler getHandler(Repository repository)
          throws RepositoryException
  {
    String type = repository.getType();
    RepositoryHandler handler = handlerMap.get(type);

    if (handler == null)
    {
      throw new RepositoryHandlerNotFoundException(
          "could not find handler for ".concat(type));
    }
    else if (!handler.isConfigured())
    {
      throw new RepositoryException("handler is not configured");
    }

    return handler;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Map<String, RepositoryHandler> handlerMap;

  /** Field description */
  private List<RepositoryType> types;
}
