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



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.api.HgHookMessageProvider;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookMessageProvider;

//~--- JDK imports ------------------------------------------------------------

import java.util.EnumSet;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgHookContextProvider extends HookContextProvider
{

  /** Field description */
  private static final Set<HookFeature> SUPPORTED_FEATURES =
    EnumSet.of(HookFeature.CHANGESET_PROVIDER, HookFeature.MESSAGE_PROVIDER);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param repositoryName
   * @param hookManager
   * @param startRev
   * @param type
   */
  public HgHookContextProvider(HgRepositoryHandler handler,
    String repositoryName, HgHookManager hookManager, String startRev,
    RepositoryHookType type)
  {
    this.hookChangesetProvider = new HgHookChangesetProvider(handler,
      repositoryName, hookManager, startRev, type);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HookChangesetProvider getChangesetProvider()
  {
    return hookChangesetProvider;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public HgHookMessageProvider getHgMessageProvider()
  {
    if (hgMessageProvider == null)
    {
      hgMessageProvider = new HgHookMessageProvider();
    }

    return hgMessageProvider;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Set<HookFeature> getSupportedFeatures()
  {
    return SUPPORTED_FEATURES;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected HookMessageProvider createMessageProvider()
  {
    return getHgMessageProvider();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgHookMessageProvider hgMessageProvider;

  /** Field description */
  private HgHookChangesetProvider hookChangesetProvider;
}
