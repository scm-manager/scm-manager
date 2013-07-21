/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.api.HookChangesetBuilder;
import sonia.scm.repository.api.HookContext;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 * The {@link ExtendedRepositoryHookEvent} at the possibility to retrieve the
 * {@link HookContext} of the current hook.
 *
 * @author Sebastian Sdorra
 * @since 1.33
 */
public class ExtendedRepositoryHookEvent extends AbstractRepositoryHookEvent
{

  /**
   * Constructs a new {@link ExtendedRepositoryHookEvent}.
   *
   * @param context context of current hook
   * @param repository
   * @param type type of current hook
   */
  public ExtendedRepositoryHookEvent(HookContext context,
    Repository repository, RepositoryHookType type)
  {
    this.context = context;
    this.repository = repository;
    this.type = type;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   * @deprecated use {@link HookChangesetBuilder#getChangesets()} instead.
   */
  @Override
  @Deprecated
  public Collection<Changeset> getChangesets()
  {
    return context.getChangesetProvider().getChangesetList();
  }

  /**
   * Returns the context of the current hook.
   *
   * @return context of current hook
   */
  public HookContext getContext()
  {
    return context;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Repository getRepository()
  {
    return repository;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RepositoryHookType getType()
  {
    return type;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   * @deprecated
   */
  @Override
  @Deprecated
  public void setRepository(Repository repository)
  {

    // do nothing
  }

  //~--- fields ---------------------------------------------------------------

  /** context of current hook */
  private HookContext context;

  /** modified repository */
  private Repository repository;

  /** hook type */
  private RepositoryHookType type;
}
