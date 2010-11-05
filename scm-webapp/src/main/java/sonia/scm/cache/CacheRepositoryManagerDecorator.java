/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import sonia.scm.ConfigChangedListener;
import sonia.scm.SCMContextProvider;
import sonia.scm.Type;
import sonia.scm.Undecorated;
import sonia.scm.repository.AbstractRepositoryManagerDecorator;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public class CacheRepositoryManagerDecorator
        extends AbstractRepositoryManagerDecorator
        implements ConfigChangedListener
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
          @Undecorated RepositoryManager manager, CacheManager cacheManager)
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
   * @param config
   */
  @Override
  public void configChanged(Object config)
  {
    cache.clear();
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
   * @param context
   */
  @Override
  public void init(SCMContextProvider context)
  {
    super.init(context);

    for (Type type : getTypes())
    {
      getHandler(type.getName()).addListener(this);
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
