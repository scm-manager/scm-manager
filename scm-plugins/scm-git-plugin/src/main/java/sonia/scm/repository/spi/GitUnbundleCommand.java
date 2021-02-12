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

import com.google.common.io.ByteSource;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.UnbundleResponse;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static sonia.scm.util.Archives.extractTar;

public class GitUnbundleCommand extends AbstractGitCommand implements UnbundleCommand {

  private static final Logger LOG = LoggerFactory.getLogger(GitUnbundleCommand.class);

  private final HookContextFactory hookContextFactory;
  private final ScmEventBus eventBus;
  private final GitChangesetConverterFactory changesetConverterFactory;

  @Inject
  GitUnbundleCommand(GitContext context, HookContextFactory hookContextFactory, ScmEventBus eventBus, GitChangesetConverterFactory changesetConverterFactory) {
    super(context);
    this.hookContextFactory = hookContextFactory;
    this.eventBus = eventBus;
    this.changesetConverterFactory = changesetConverterFactory;
  }

  @Override
  public UnbundleResponse unbundle(UnbundleCommandRequest request) throws IOException {
    ByteSource archive = checkNotNull(request.getArchive(),"archive is required");
    Path repositoryDir = context.getDirectory().toPath();
    LOG.debug("archive repository {} to {}", repositoryDir, archive);

    if (!Files.exists(repositoryDir)) {
      Files.createDirectories(repositoryDir);
    }

    unbundleRepositoryFromRequest(request, repositoryDir);
    firePostReceiveRepositoryHookEvent();

    return new UnbundleResponse(0);
  }

  private void firePostReceiveRepositoryHookEvent() {
    try {
      Repository repository = context.open();
      Git git = Git.wrap(repository);
      List<String> branches = extractBranches(git);
      List<Tag> tags = extractTags(git);
      Iterable<RevCommit> changesets = extractChangesets(git);
      eventBus.post(createEvent(changesetConverterFactory.create(repository), branches, tags, changesets));
    } catch (IOException | GitAPIException e) {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.entity(context.getRepository()).build(),
        "Could not fire post receive repository hook event after unbundle",
        e
      );
    }
  }

  private Iterable<RevCommit> extractChangesets(Git git) throws GitAPIException, IOException {
    return git.log().all().call();
  }

  private List<Tag> extractTags(Git git) throws GitAPIException {
    return git.tagList().call().stream().map(r -> new Tag(r.getName(), r.getObjectId().toString())).collect(Collectors.toList());
  }

  private List<String> extractBranches(Git git) throws GitAPIException {
    return git.branchList().call().stream()
      .map(Ref::getName)
      .collect(Collectors.toList());
  }

  private PostReceiveRepositoryHookEvent createEvent(GitChangesetConverter converter, List<String> branches, List<Tag> tags, Iterable<RevCommit> changesets) {
    HookContext context = hookContextFactory.createContext(new GitImportHookContextProvider(converter, tags, changesets, branches), this.context.getRepository());
    RepositoryHookEvent repositoryHookEvent = new RepositoryHookEvent(context, this.context.getRepository(), RepositoryHookType.POST_RECEIVE);
    return new PostReceiveRepositoryHookEvent(repositoryHookEvent);
  }

  private void unbundleRepositoryFromRequest(UnbundleCommandRequest request, Path repositoryDir) throws IOException {
    extractTar(request.getArchive().openBufferedStream(), repositoryDir).run();
  }
}
