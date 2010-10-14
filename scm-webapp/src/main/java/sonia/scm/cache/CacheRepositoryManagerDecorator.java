/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.name.Named;

import sonia.scm.repository.AbstractRepositoryManagerDecorator;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import sonia.scm.Undecorated;

/**
 *
 * @author Sebastian Sdorra
 */
public class CacheRepositoryManagerDecorator
        extends AbstractRepositoryManagerDecorator
{

  /** Field description */
  public static final String CACHE_KEY_ALL = "__all";

  /** Field description */
  public static final String CACHE_REPOSITORY = "sonia.cache.repository";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param manager
   * @param cacheManager
   */
  @Inject
  public CacheRepositoryManagerDecorator(
          @Undecorated RepositoryManager manager,
          CacheManager cacheManager)
  {
    super(manager);
    cache = cacheManager.getExtendedCache(String.class, Repository.class,
            CACHE_REPOSITORY);
  }

  //~--- methods --------------------------------------------------------------

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
    orginal.create(repository);
    putToCache(repository);
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
    orginal.delete(repository);
    removeFromCache(repository);
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
    cache.remove(repository.getId());
    orginal.modify(repository);
    putToCache(repository);
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
    orginal.refresh(repository);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public Repository get(String id)
  {
    AssertUtil.assertIsNotEmpty(id);

    Repository repository = cache.get(id);

    if (repository == null)
    {
      repository = orginal.get(id);

      if (repository != null)
      {
        putToCache(repository);
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
    Collection<Repository> repositories = cache.getCollection(CACHE_KEY_ALL);

    if (repositories == null)
    {
      repositories = orginal.getAll();
      cache.putCollection(CACHE_KEY_ALL, repositories);
    }

    return repositories;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   */
  private void putToCache(Repository repository)
  {
    String id = repository.getId();

    if (Util.isNotEmpty(id))
    {
      cache.put(id, repository);
      cache.removeCollection(CACHE_KEY_ALL);
    }
  }

  /**
   * Method description
   *
   *
   * @param repository
   */
  private void removeFromCache(Repository repository)
  {
    cache.remove(repository.getId());
    cache.removeCollection(CACHE_KEY_ALL);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ExtendedCache<String, Repository> cache;
}
