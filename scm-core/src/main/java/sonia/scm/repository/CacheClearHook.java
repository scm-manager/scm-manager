/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
