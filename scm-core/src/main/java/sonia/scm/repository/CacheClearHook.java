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

//~--- non-JDK imports --------------------------------------------------------

import com.github.legman.Subscribe;

import com.google.common.base.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.cache.Cache;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.6
 */
public class CacheClearHook
{

  /** the logger for CacheClearHook */
  private static final Logger logger =
    LoggerFactory.getLogger(CacheClearHook.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   * @since 1.7
   *
   */
  public void clearCache()
  {
    clearCache(null);
  }

  /**
   * Method description
   *
   * @since 1.9
   *
   * @param predicate
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

  /**
   * Method description
   *
   *
   * @param event
   */
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
   * Method description
   *
   * @since 1.9
   *
   *
   * @param event
   * @return
   */
  protected Predicate<?> createPredicate(RepositoryHookEvent event)
  {
    return null;
  }

  /**
   * Method description
   *
   *
   * @param cache
   */
  protected void init(Cache<?, ?> cache)
  {
    this.cache = cache;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Cache<?, ?> cache;
}
