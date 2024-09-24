/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository;


import com.github.legman.Subscribe;

import com.google.common.base.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.cache.Cache;

/**
 *
 * @since 1.6
 */
public class CacheClearHook
{
  private static final Logger logger =
    LoggerFactory.getLogger(CacheClearHook.class);

  private Cache<?, ?> cache;

  /**
   * @since 1.7
   */
  public void clearCache()
  {
    clearCache(null);
  }

  /**
   * @since 1.9
   */
  @SuppressWarnings("unchecked")
  public void clearCache(Predicate predicate)
  {
    if (predicate != null)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("clear cache, with filter");
      }

      cache.removeAll(predicate);
    }
    else
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("clear cache");
      }

      cache.clear();
    }
  }


  @Subscribe
  public void onEvent(PostReceiveRepositoryHookEvent event)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("clear cache because repository {} has changed",
        event.getRepository().getName());
    }

    clearCache(createPredicate(event));
  }

  /**
   * @since 1.9
   */
  protected Predicate<?> createPredicate(RepositoryHookEvent event)
  {
    return null;
  }


  protected void init(Cache<?, ?> cache)
  {
    this.cache = cache;
  }

}
