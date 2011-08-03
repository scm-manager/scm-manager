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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.cache.Cache;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.6
 */
public class CacheClearHook implements RepositoryHook
{

  /** the logger for CacheClearHook */
  private static final Logger logger =
    LoggerFactory.getLogger(CacheClearHook.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param event
   */
  @Override
  public void onEvent(RepositoryHookEvent event)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("clear cache because repository {} has changed",
                   event.getRepository().getName());
    }

    cache.clear();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<RepositoryHookType> getTypes()
  {
    return types;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isAsync()
  {
    return true;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param manager
   * @param cache
   */
  protected void init(RepositoryManager manager, Cache<?, ?> cache)
  {
    manager.addHook(this);
    this.cache = cache;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Cache<?, ?> cache;

  /** Field description */
  private Collection<RepositoryHookType> types =
    Arrays.asList(RepositoryHookType.POST_RECEIVE);
}
