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

import sonia.scm.HandlerEvent;
import sonia.scm.util.AssertUtil;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractRepositoryManager implements RepositoryManager
{

  /**
   * Method description
   *
   *
   * @param hook
   * @param repository
   * @param changesets
   */
  protected abstract void firePostReceiveEvent(PostReceiveHook hook,
          Repository repository, List<Changeset> changesets);

  /**
   * Method description
   *
   *
   * @param listener
   */
  @Override
  public void addListener(RepositoryListener listener)
  {
    listenerSet.add(listener);
  }

  /**
   * Method description
   *
   *
   * @param listeners
   */
  @Override
  public void addListeners(Collection<RepositoryListener> listeners)
  {
    listenerSet.addAll(listeners);
  }

  /**
   * Method description
   *
   *
   * @param hook
   */
  @Override
  public void addPostReceiveHook(PostReceiveHook hook)
  {
    postReceiveHookSet.add(hook);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param changesets
   */
  @Override
  public void firePostReceiveEvent(Repository repository,
                                   List<Changeset> changesets)
  {
    AssertUtil.assertIsNotNull(repository);
    AssertUtil.assertIsNotEmpty(changesets);

    for (PostReceiveHook hook : postReceiveHookSet)
    {
      firePostReceiveEvent(hook, repository, changesets);
    }
  }

  /**
   * Method description
   *
   *
   * @param listener
   */
  @Override
  public void removeListener(RepositoryListener listener)
  {
    listenerSet.remove(listener);
  }

  /**
   * Method description
   *
   *
   * @param hook
   */
  @Override
  public void removePostReceiveHook(PostReceiveHook hook)
  {
    removePostReceiveHook(hook);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param event
   */
  protected void fireEvent(Repository repository, HandlerEvent event)
  {
    for (RepositoryListener listener : listenerSet)
    {
      listener.onEvent(repository, event);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<PostReceiveHook> postReceiveHookSet =
    new HashSet<PostReceiveHook>();

  /** Field description */
  private Set<RepositoryListener> listenerSet =
    new HashSet<RepositoryListener>();
}
