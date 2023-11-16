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

package sonia.scm.repository.spi;

import sonia.scm.repository.HgConfigResolver;
import sonia.scm.repository.HgRepositoryFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HgHookBranchProvider;
import sonia.scm.repository.api.HgHookMessageProvider;
import sonia.scm.repository.api.HgHookTagProvider;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookChangesetProvider;
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
public class HgHookContextProvider extends HookContextProvider {

  private static final Set<HookFeature> SUPPORTED_FEATURES = EnumSet.of(
    HookFeature.CHANGESET_PROVIDER,
    HookFeature.MESSAGE_PROVIDER,
    HookFeature.BRANCH_PROVIDER,
    HookFeature.TAG_PROVIDER
  );

  private final HgHookChangesetProvider hookChangesetProvider;
  private HgHookMessageProvider hgMessageProvider;
  private HgHookBranchProvider hookBranchProvider;
  private HgHookTagProvider hookTagProvider;

  public HgHookContextProvider(HgConfigResolver configResolver, HgRepositoryFactory factory, Repository repository, String startRev) {
    this.hookChangesetProvider = new HgHookChangesetProvider(configResolver, factory, repository, startRev);
  }

  @Override
  public HookBranchProvider getBranchProvider() {
    if (hookBranchProvider == null) {
      hookBranchProvider = new HgHookBranchProvider(hookChangesetProvider);
    }
    return hookBranchProvider;
  }

  @Override
  public HookTagProvider getTagProvider() {
    if (hookTagProvider == null) {
      hookTagProvider = new HgHookTagProvider(hookChangesetProvider);
    }
    return hookTagProvider;
  }

  @Override
  public HookChangesetProvider getChangesetProvider()
  {
    return hookChangesetProvider;
  }

  public HgHookMessageProvider getHgMessageProvider() {
    if (hgMessageProvider == null) {
      hgMessageProvider = new HgHookMessageProvider();
    }
    return hgMessageProvider;
  }

  @Override
  public Set<HookFeature> getSupportedFeatures()
  {
    return SUPPORTED_FEATURES;
  }

  @Override
  protected HookMessageProvider createMessageProvider()
  {
    return getHgMessageProvider();
  }
}
