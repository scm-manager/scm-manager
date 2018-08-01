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
import sonia.scm.repository.api.HgHookBranchProvider;
import sonia.scm.repository.api.HgHookMessageProvider;
import sonia.scm.repository.api.HgHookTagProvider;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookMessageProvider;
import sonia.scm.repository.api.HookTagProvider;

import java.util.EnumSet;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * Mercurial implementation of {@link HookContextProvider}.
 * 
 * @author Sebastian Sdorra
 */
public class HgHookContextProvider extends HookContextProvider
{

  private static final Set<HookFeature> SUPPORTED_FEATURES =
    EnumSet.of(HookFeature.CHANGESET_PROVIDER, HookFeature.MESSAGE_PROVIDER,
      HookFeature.BRANCH_PROVIDER, HookFeature.TAG_PROVIDER);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new instance.
   *
   * @param handler mercurial repository handler
   * @param namespaceAndName namespace and name of changed repository
   * @param hookManager mercurial hook manager
   * @param startRev start revision
   * @param type type of hook
   */
  public HgHookContextProvider(HgRepositoryHandler handler,
    String id, HgHookManager hookManager, String startRev,
    RepositoryHookType type)
  {
    this.hookChangesetProvider = new HgHookChangesetProvider(handler, id, hookManager, startRev, type);
  }

  //~--- get methods ----------------------------------------------------------

  @Override
  public HookBranchProvider getBranchProvider()
  {
    if (hookBranchProvider == null)
    {
      hookBranchProvider = new HgHookBranchProvider(hookChangesetProvider);
    }

    return hookBranchProvider;
  }

  @Override
  public HookTagProvider getTagProvider() 
  {
    if (hookTagProvider == null)
    {
      hookTagProvider = new HgHookTagProvider(hookChangesetProvider);
    }
    
    return hookTagProvider;
  }

  @Override
  public HookChangesetProvider getChangesetProvider()
  {
    return hookChangesetProvider;
  }
  
  public HgHookMessageProvider getHgMessageProvider()
  {
    if (hgMessageProvider == null)
    {
      hgMessageProvider = new HgHookMessageProvider();
    }

    return hgMessageProvider;
  }

  @Override
  public Set<HookFeature> getSupportedFeatures()
  {
    return SUPPORTED_FEATURES;
  }

  //~--- methods --------------------------------------------------------------

  @Override
  protected HookMessageProvider createMessageProvider()
  {
    return getHgMessageProvider();
  }

  //~--- fields ---------------------------------------------------------------

  private final HgHookChangesetProvider hookChangesetProvider;

  private HgHookMessageProvider hgMessageProvider;

  private HgHookBranchProvider hookBranchProvider;
  
  private HgHookTagProvider hookTagProvider;
}
