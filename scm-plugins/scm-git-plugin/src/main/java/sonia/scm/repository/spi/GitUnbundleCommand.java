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
import com.google.inject.assistedinject.Assisted;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.Tag;
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

  private final GitRepositoryHookEventFactory eventFactory;

  @Inject
  GitUnbundleCommand(@Assisted GitContext context, GitRepositoryHookEventFactory eventFactory) {
    super(context);
    this.eventFactory = eventFactory;
  }

  @Override
  public UnbundleResponse unbundle(UnbundleCommandRequest request) throws IOException {
    ByteSource archive = checkNotNull(request.getArchive(), "archive is required");
    Path repositoryDir = context.getDirectory().toPath();
    LOG.debug("archive repository {} to {}", repositoryDir, archive);

    if (!Files.exists(repositoryDir)) {
      Files.createDirectories(repositoryDir);
    }

    unbundleRepositoryFromRequest(request, repositoryDir);
    fireRepositoryHookEvent(request);

    return new UnbundleResponse(0);
  }

  private void fireRepositoryHookEvent(UnbundleCommandRequest request) {
    try {
      Git git = Git.wrap(context.open());
      List<String> branches = extractBranches(git);
      List<Tag> tags = extractTags(git);
      GitLazyChangesetResolver changesetResolver = new GitLazyChangesetResolver(context.getRepository(), git);
      RepositoryHookEvent event = eventFactory.createEvent(context, branches, tags, changesetResolver);
      if (event != null) {
        request.getPostEventSink().accept(event);
      }
    } catch (IOException | GitAPIException e) {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.entity(context.getRepository()).build(),
        "Could not fire post receive repository hook event after unbundle",
        e
      );
    }
  }

  private List<Tag> extractTags(Git git) throws GitAPIException {
    return git.tagList().call().stream()
      .map(r -> new Tag(r.getName(), r.getObjectId().getName()))
      .collect(Collectors.toList());
  }

  private List<String> extractBranches(Git git) throws GitAPIException {
    return git.branchList().call().stream()
      .map(Ref::getName)
      .collect(Collectors.toList());
  }

  private void unbundleRepositoryFromRequest(UnbundleCommandRequest request, Path repositoryDir) throws IOException {
    extractTar(request.getArchive().openBufferedStream(), repositoryDir).run();
  }

  public interface Factory {
    UnbundleCommand create(GitContext context);
  }

}
