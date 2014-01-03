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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.HandlerEvent;
import sonia.scm.event.ScmEventBus;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for {@link RepositoryManager} implementations. This class
 * implements the listener and hook methods of the {@link RepositoryManager}
 * interface.
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractRepositoryManager implements RepositoryManager
{

  /** the logger for AbstractRepositoryManager */
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractRepositoryManager.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Sends a {@link RepositoryHookEvent} to the specified
   * {@link RepositoryHook}.
   *
   *
   * @param hook receiving repository hook
   * @param event hook event
   */
  protected abstract void fireHookEvent(RepositoryHook hook,
    RepositoryHookEvent event);

  /**
   * Registers a {@link RepositoryHook}
   *
   *
   * @param hook hook to register
   */
  @Override
  public void addHook(RepositoryHook hook)
  {
    Collection<RepositoryHookType> types = hook.getTypes();

    if (types != null)
    {
      for (RepositoryHookType type : types)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("register {} hook {}", type, hook.getClass());
        }

        synchronized (AbstractRepositoryManager.class)
        {
          List<RepositoryHook> hooks = hookMap.get(type);

          if (hooks == null)
          {
            hooks = new ArrayList<RepositoryHook>();
            hookMap.put(type, hooks);
          }

          hooks.add(hook);
        }
      }
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("could not find any repository type");
    }
  }

  /**
   * Register a {@link Collection} of {@link RepositoryHook}s
   *
   *
   * @param hooks hooks to register
   */
  @Override
  public void addHooks(Collection<RepositoryHook> hooks)
  {
    for (RepositoryHook hook : hooks)
    {
      addHook(hook);
    }
  }


  /**
   * Sends a {@link RepositoryHookEvent} to each registered
   * {@link RepositoryHook} and sends the {@link RepositoryHookEvent} to
   * the {@link ScmEventBus}.
   *
   *
   * @param repository changed repository
   * @param event event to be fired
   */
  @Override
  public void fireHookEvent(Repository repository, RepositoryHookEvent event)
  {
    AssertUtil.assertIsNotNull(repository);
    AssertUtil.assertIsNotNull(event);
    AssertUtil.assertIsNotNull(event.getType());
    event.setRepository(repository);

    // prepare the event
    event = prepareHookEvent(event);

    // post wrapped hook to event system
    ScmEventBus.getInstance().post(WrappedRepositoryHookEvent.wrap(event));

    List<RepositoryHook> hooks = hookMap.get(event.getType());

    if (Util.isNotEmpty(hooks))
    {
      for (RepositoryHook hook : hooks)
      {
        fireHookEvent(hook, event);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param hook
   */
  @Override
  public void removeHook(RepositoryHook hook)
  {
    Collection<RepositoryHookType> types = hook.getTypes();

    if (types != null)
    {
      for (RepositoryHookType type : types)
      {
        List<RepositoryHook> hooks = hookMap.get(type);

        if (hooks != null)
        {
          hooks.remove(hook);
        }
      }
    }
  }

  /**
   * Send a {@link RepositoryEvent} to the {@link ScmEventBus}.
   *
   * @param repository repository that has changed
   * @param event type of change event
   */
  protected void fireEvent(Repository repository, HandlerEvent event)
  {
    ScmEventBus.getInstance().post(new RepositoryEvent(repository, event));
  }

  /**
   * Prepare a hook event before it is fired to the event system of SCM-Manager.
   *
   *
   * @param event hook event
   * @since 1.26
   *
   * @return
   */
  protected RepositoryHookEvent prepareHookEvent(RepositoryHookEvent event)
  {
    return event;
  }

  //~--- fields ---------------------------------------------------------------

  /** repository hooks map */
  private Map<RepositoryHookType, List<RepositoryHook>> hookMap =
    Maps.newEnumMap(RepositoryHookType.class);
}
