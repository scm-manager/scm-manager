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

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.api.GitHookBranchProvider;
import sonia.scm.repository.api.GitHookMessageProvider;
import sonia.scm.repository.api.GitHookTagProvider;
import sonia.scm.repository.api.GitReceiveHookMergeDetectionProvider;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookMessageProvider;
import sonia.scm.repository.api.HookTagProvider;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitHookContextProvider extends HookContextProvider
{

  /**
   * Field description
   */
  private static final Set<HookFeature> SUPPORTED_FEATURES = EnumSet.of(
    HookFeature.MESSAGE_PROVIDER,
    HookFeature.CHANGESET_PROVIDER,
    HookFeature.BRANCH_PROVIDER,
    HookFeature.TAG_PROVIDER,
    HookFeature.MERGE_DETECTION_PROVIDER
  );

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new instance
   *  @param receivePack git receive pack
   * @param receiveCommands received commands
   * @param type
   */
  public GitHookContextProvider(
    ReceivePack receivePack,
    List<ReceiveCommand> receiveCommands,
    RepositoryHookType type,
    Repository repository,
    String repositoryId
  ) {
    this.receivePack = receivePack;
    this.receiveCommands = receiveCommands;
    this.type = type;
    this.repository = repository;
    this.repositoryId = repositoryId;
    this.changesetProvider = new GitHookChangesetProvider(receivePack,
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
  public HookMergeDetectionProvider getMergeDetectionProvider() {
    if (type == RepositoryHookType.POST_RECEIVE) {
      return new GitReceiveHookMergeDetectionProvider(repository, repositoryId, branch -> branch);
    } else {
      return new GitReceiveHookMergeDetectionProvider(repository, repositoryId, this::findNewRevisionForBranchIfToBeUpdated);
    }
  }

  private String findNewRevisionForBranchIfToBeUpdated(String branch) {
    return receiveCommands
      .stream()
      .filter(receiveCommand -> isReceiveCommandForBranch(branch, receiveCommand))
      .map(ReceiveCommand::getNewId)
      .map(AnyObjectId::getName)
      .findFirst()
      .orElse(branch);
  }

  private boolean isReceiveCommandForBranch(String branch, ReceiveCommand receiveCommand) {
    return GitUtil.getBranch(receiveCommand.getRef()).equals(branch);
  }

  @Override
  public Set<HookFeature> getSupportedFeatures()
  {
    return SUPPORTED_FEATURES;
  }

  private final GitHookChangesetProvider changesetProvider;
  private final List<ReceiveCommand> receiveCommands;
  private final ReceivePack receivePack;
  private final RepositoryHookType type;
  private final Repository repository;
  private final String repositoryId;
}
