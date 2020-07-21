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

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.api.GitHookBranchProvider;
import sonia.scm.repository.api.GitHookMessageProvider;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookMessageProvider;

//~--- JDK imports ------------------------------------------------------------

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import sonia.scm.repository.api.GitHookTagProvider;
import sonia.scm.repository.api.HookTagProvider;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitHookContextProvider extends HookContextProvider
{

  /** Field description */
  private static final Set<HookFeature> SUPPORTED_FEATURES =
    EnumSet.of(HookFeature.MESSAGE_PROVIDER, HookFeature.CHANGESET_PROVIDER,
      HookFeature.BRANCH_PROVIDER, HookFeature.TAG_PROVIDER);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new instance
   *
   * @param receivePack git receive pack
   * @param receiveCommands received commands
   */
  public GitHookContextProvider(GitChangesetConverterFactory converterFactory, ReceivePack receivePack,
                                List<ReceiveCommand> receiveCommands)
  {
    this.receivePack = receivePack;
    this.receiveCommands = receiveCommands;
    this.changesetProvider = new GitHookChangesetProvider(converterFactory, receivePack,
      receiveCommands);
  }

  //~--- methods --------------------------------------------------------------

  @Override
  public HookMessageProvider createMessageProvider()
  {
    return new GitHookMessageProvider(receivePack);
  }

  //~--- get methods ----------------------------------------------------------

  @Override
  public HookBranchProvider getBranchProvider()
  {
    return new GitHookBranchProvider(receiveCommands);
  }

  @Override
  public HookTagProvider getTagProvider() {
    return new GitHookTagProvider(receiveCommands);
  }

  @Override
  public HookChangesetProvider getChangesetProvider()
  {
    return changesetProvider;
  }

  @Override
  public Set<HookFeature> getSupportedFeatures()
  {
    return SUPPORTED_FEATURES;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final GitHookChangesetProvider changesetProvider;

  /** Field description */
  private final List<ReceiveCommand> receiveCommands;

  /** Field description */
  private final ReceivePack receivePack;
}
