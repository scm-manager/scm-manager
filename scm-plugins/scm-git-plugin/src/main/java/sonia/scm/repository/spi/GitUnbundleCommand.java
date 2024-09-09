/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository.spi;

import com.google.common.io.ByteSource;
import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
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
