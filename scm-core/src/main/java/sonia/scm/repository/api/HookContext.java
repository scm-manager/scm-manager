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



package sonia.scm.repository.api;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.Changeset;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.HookContextProvider;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.33
 */
public final class HookContext
{

  /**
   * Constructs ...
   *
   *
   * @param provider
   * @param repository
   * @param preProcessorUtil
   */
  HookContext(HookContextProvider provider, Repository repository,
    PreProcessorUtil preProcessorUtil)
  {
    this.provider = provider;
    this.repository = repository;
    this.preProcessorUtil = preProcessorUtil;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a {@link HookChangesetBuilder} which is able to return all 
   * {@link Changeset}'s during this push/commit.
   *
   *
   * @return {@link HookChangesetBuilder}
   */
  public HookChangesetBuilder getChangesetProvider()
  {
    //J-
    return new HookChangesetBuilder(
      repository, 
      preProcessorUtil,
      provider.getChangesetProvider()
    );
    //J+
  }

  /**
   * Returns a {@link HookMessageProvider} which is able to send message back to
   * the scm client.
   * <strong>Note:</strong> The {@link HookMessageProvider} is only available if
   * the underlying {@link HookContextProvider} supports the handling of
   * messages and the hook is executed synchronous.
   *
   * @return {@link HookMessageProvider} which is able to send message back to
   * the scm client
   */
  public HookMessageProvider getMessageProvider()
  {
    return provider.getMessageProvider();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private PreProcessorUtil preProcessorUtil;

  /** Field description */
  private HookContextProvider provider;

  /** Field description */
  private Repository repository;
}
