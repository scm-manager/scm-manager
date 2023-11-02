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

package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.servlet.ServletModule;
import org.eclipse.jgit.transport.ScmTransportProtocol;
import org.mapstruct.factory.Mappers;
import sonia.scm.api.v2.resources.GitConfigDtoToGitConfigMapper;
import sonia.scm.api.v2.resources.GitConfigToGitConfigDtoMapper;
import sonia.scm.api.v2.resources.GitRepositoryConfigMapper;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.GitWorkingCopyFactory;
import sonia.scm.repository.spi.BlameCommand;
import sonia.scm.repository.spi.BranchCommand;
import sonia.scm.repository.spi.BranchDetailsCommand;
import sonia.scm.repository.spi.BranchesCommand;
import sonia.scm.repository.spi.BrowseCommand;
import sonia.scm.repository.spi.BundleCommand;
import sonia.scm.repository.spi.CatCommand;
import sonia.scm.repository.spi.ChangesetsCommand;
import sonia.scm.repository.spi.DiffCommand;
import sonia.scm.repository.spi.DiffResultCommand;
import sonia.scm.repository.spi.FileLockCommand;
import sonia.scm.repository.spi.GitBlameCommand;
import sonia.scm.repository.spi.GitBranchCommand;
import sonia.scm.repository.spi.GitBranchDetailsCommand;
import sonia.scm.repository.spi.GitBranchesCommand;
import sonia.scm.repository.spi.GitBrowseCommand;
import sonia.scm.repository.spi.GitBundleCommand;
import sonia.scm.repository.spi.GitCatCommand;
import sonia.scm.repository.spi.GitChangesetsCommand;
import sonia.scm.repository.spi.GitDiffCommand;
import sonia.scm.repository.spi.GitDiffResultCommand;
import sonia.scm.repository.spi.GitFileLockCommand;
import sonia.scm.repository.spi.GitIncomingCommand;
import sonia.scm.repository.spi.GitLogCommand;
import sonia.scm.repository.spi.GitMergeCommand;
import sonia.scm.repository.spi.GitMirrorCommand;
import sonia.scm.repository.spi.GitModificationsCommand;
import sonia.scm.repository.spi.GitModifyCommand;
import sonia.scm.repository.spi.GitOutgoingCommand;
import sonia.scm.repository.spi.GitPullCommand;
import sonia.scm.repository.spi.GitPushCommand;
import sonia.scm.repository.spi.GitTagCommand;
import sonia.scm.repository.spi.GitTagsCommand;
import sonia.scm.repository.spi.GitUnbundleCommand;
import sonia.scm.repository.spi.IncomingCommand;
import sonia.scm.repository.spi.LogCommand;
import sonia.scm.repository.spi.MergeCommand;
import sonia.scm.repository.spi.MirrorCommand;
import sonia.scm.repository.spi.ModificationsCommand;
import sonia.scm.repository.spi.ModifyCommand;
import sonia.scm.repository.spi.OutgoingCommand;
import sonia.scm.repository.spi.PostReceiveRepositoryHookEventFactory;
import sonia.scm.repository.spi.PullCommand;
import sonia.scm.repository.spi.PushCommand;
import sonia.scm.repository.spi.SimpleGitWorkingCopyFactory;
import sonia.scm.repository.spi.TagCommand;
import sonia.scm.repository.spi.TagsCommand;
import sonia.scm.repository.spi.UnbundleCommand;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

/**
 * @author Sebastian Sdorra
 */
@Extension
public class GitServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    bind(GitRepositoryViewer.class);
    bind(GitRepositoryResolver.class);
    bind(GitReceivePackFactory.class);
    bind(ScmTransportProtocol.class);

    bind(LfsBlobStoreFactory.class);

    bind(GitConfigDtoToGitConfigMapper.class).to(Mappers.getMapper(GitConfigDtoToGitConfigMapper.class).getClass());
    bind(GitConfigToGitConfigDtoMapper.class).to(Mappers.getMapper(GitConfigToGitConfigDtoMapper.class).getClass());
    bind(GitRepositoryConfigMapper.class).to(Mappers.getMapper(GitRepositoryConfigMapper.class).getClass());

    bind(GitWorkingCopyFactory.class).to(SimpleGitWorkingCopyFactory.class);

    install(new FactoryModuleBuilder().implement(BranchCommand.class, GitBranchCommand.class).build(GitBranchCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(BlameCommand.class, GitBlameCommand.class).build(GitBlameCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(BranchesCommand.class, GitBranchesCommand.class).build(GitBranchesCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(BrowseCommand.class, GitBrowseCommand.class).build(GitBrowseCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(CatCommand.class, GitCatCommand.class).build(GitCatCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(DiffCommand.class, GitDiffCommand.class).build(GitDiffCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(DiffResultCommand.class, GitDiffResultCommand.class).build(GitDiffResultCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(IncomingCommand.class, GitIncomingCommand.class).build(GitIncomingCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(LogCommand.class, GitLogCommand.class).build(GitLogCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(ModificationsCommand.class, GitModificationsCommand.class).build(GitModificationsCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(OutgoingCommand.class, GitOutgoingCommand.class).build(GitOutgoingCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(PullCommand.class, GitPullCommand.class).build(GitPullCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(PostReceiveRepositoryHookEventFactory.class, PostReceiveRepositoryHookEventFactory.class).build(PostReceiveRepositoryHookEventFactory.Factory.class));
    install(new FactoryModuleBuilder().implement(PushCommand.class, GitPushCommand.class).build(GitPushCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(TagsCommand.class, GitTagsCommand.class).build(GitTagsCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(TagCommand.class, GitTagCommand.class).build(GitTagCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(MergeCommand.class, GitMergeCommand.class).build(GitMergeCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(ModifyCommand.class, GitModifyCommand.class).build(GitModifyCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(BundleCommand.class, GitBundleCommand.class).build(GitBundleCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(UnbundleCommand.class, GitUnbundleCommand.class).build(GitUnbundleCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(MirrorCommand.class, GitMirrorCommand.class).build(GitMirrorCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(FileLockCommand.class, GitFileLockCommand.class).build(GitFileLockCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(BranchDetailsCommand.class, GitBranchDetailsCommand.class).build(GitBranchDetailsCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(ChangesetsCommand.class, GitChangesetsCommand.class).build(GitChangesetsCommand.Factory.class));


  }
}
