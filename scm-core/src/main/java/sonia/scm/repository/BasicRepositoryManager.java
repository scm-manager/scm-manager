/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(BasicRepositoryManager.class);

  //~--- constructors ---------------------------------------------------------

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

    if (logger.isInfoEnabled())
    {
      logger.info("added RepositoryHandler {} for type {}", handler.getClass(),
                  type);
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
    if (logger.isInfoEnabled())
    {
      logger.info("create repository {} of type {}", repository.getName(),
                  repository.getType());
    }

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
    if (logger.isInfoEnabled())
    {
      logger.info("delete repository {} of type {}", repository.getName(),
                  repository.getType());
    }

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
    if (logger.isInfoEnabled())
    {
      logger.info("modify repository {} of type {}", repository.getName(),
                  repository.getType());
    }

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
    if (logger.isDebugEnabled())
    {
      logger.debug("fetch all repositories");
    }

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

    if (logger.isDebugEnabled())
    {
      logger.debug("fetched {} repositories", repositories.size());
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
