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

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.6
 */
public interface RepositoryHookSupport
{

  /**
   * Registers a new {@link RepositoryHook}.
   *
   *
   * @param hook to register
   */
  public void addHook(RepositoryHook hook);

  /**
   * Register a {@link java.util.Collection} of hooks.
   *
   * @param hooks to register
   */
  public void addHooks(Collection<RepositoryHook> hooks);

  /**
   * Fires a post receive hook event. This methods calls the
   * {@link PostReceiveHook#onPostReceive(Repository, List)} of each registered
   * {@link PostReceiveHook}.
   *
   *
   * @param repository that has changed
   * @param event
   */
  public void fireHookEvent(Repository repository, RepositoryHookEvent event);

  /**
   * Fires a post receive hook event. This methods calls the
   * {@link PostReceiveHook#onPostReceive(Repository, List)} of each registered
   * {@link PostReceiveHook}.
   *
   *
   * @param type of the repository
   * @param name of the repository
   * @param event
   *
   * @throws RepositoryNotFoundException if the repository could not be found.
   */
  public void fireHookEvent(String type, String name, RepositoryHookEvent event)
          throws RepositoryNotFoundException;

  /**
   * Fires a hook event. This methods calls the
   * {@link RepositoryHook#onEvent(RepositoryHookEvent} of each registered
   * {@link RepositoryHook}.
   *
   *
   * @param id of the repository
   * @param event
   *
   * @throws RepositoryNotFoundException if the repository could not be found
   */
  public void fireHookEvent(String id, RepositoryHookEvent event)
          throws RepositoryNotFoundException;

  /**
   * Unregisters the given {@link RepositoryHook}.
   *
   *
   * @param hook to unregister
   */
  public void removeHook(RepositoryHook hook);
}
