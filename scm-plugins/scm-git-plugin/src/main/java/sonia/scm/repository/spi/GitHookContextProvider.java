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


import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import sonia.scm.repository.api.GitHookBranchProvider;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.api.GitHookModificationsProvider;
import sonia.scm.repository.api.GitHookMessageProvider;
import sonia.scm.repository.api.GitHookTagProvider;
import sonia.scm.repository.api.GitReceiveHookMergeDetectionProvider;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookChangesetProvider;
import sonia.scm.repository.api.HookModificationsProvider;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookMessageProvider;
import sonia.scm.repository.api.HookTagProvider;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;


public class GitHookContextProvider extends HookContextProvider
{

  
  private static final Set<HookFeature> SUPPORTED_FEATURES = EnumSet.of(
    HookFeature.MESSAGE_PROVIDER,
    HookFeature.CHANGESET_PROVIDER,
    HookFeature.BRANCH_PROVIDER,
    HookFeature.TAG_PROVIDER,
    HookFeature.MODIFICATIONS_PROVIDER,
    HookFeature.MERGE_DETECTION_PROVIDER
  );


  private final GitChangesetConverterFactory converterFactory;

  public GitHookContextProvider(
    GitChangesetConverterFactory converterFactory, ReceivePack receivePack,
                                List<ReceiveCommand> receiveCommands,
    Repository repository,
    String repositoryId
  ) {
    this.receivePack = receivePack;
    this.receiveCommands = receiveCommands;
    this.repository = repository;
    this.repositoryId = repositoryId;
    this.changesetProvider = new GitHookChangesetProvider(converterFactory, receivePack,
      receiveCommands);
    this.converterFactory = converterFactory;
  }


  @Override
  public HookMessageProvider createMessageProvider()
  {
    return new GitHookMessageProvider(receivePack);
  }


  @Override
  public HookBranchProvider getBranchProvider()
  {
    return new GitHookBranchProvider(receiveCommands);
  }

  @Override
  public HookTagProvider getTagProvider() {
    return new GitHookTagProvider(receiveCommands, repository);
  }

  @Override
  public HookChangesetProvider getChangesetProvider()
  {
    return changesetProvider;
  }

  @Override
  public HookMergeDetectionProvider getMergeDetectionProvider() {
    return new GitReceiveHookMergeDetectionProvider(repository, repositoryId, receiveCommands, converterFactory);
  }

  @Override
  public HookModificationsProvider getModificationsProvider() {
    return new GitHookModificationsProvider(receiveCommands, repository);
  }

  @Override
  public Set<HookFeature> getSupportedFeatures()
  {
    return SUPPORTED_FEATURES;
  }

  public String getRepositoryId() {
    return repositoryId;
  }

  private final GitHookChangesetProvider changesetProvider;
  private final List<ReceiveCommand> receiveCommands;
  private final ReceivePack receivePack;
  private final Repository repository;
  private final String repositoryId;
}
