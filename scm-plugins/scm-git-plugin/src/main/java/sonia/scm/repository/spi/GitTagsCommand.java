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


import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevWalk;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Tag;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;


public class GitTagsCommand extends AbstractGitCommand implements TagsCommand {

  private final GitTagConverter gitTagConverter;


  @Inject
  public GitTagsCommand(@Assisted GitContext context, GitTagConverter gitTagConverter) {
    super(context);
    this.gitTagConverter = gitTagConverter;
  }


  @Override
  public List<Tag> getTags() throws IOException {
    return getTags(null);
  }

  @Override
  public List<Tag> getTags(String revision) throws IOException {
    try (Git git = new Git(open()); RevWalk revWalk = new RevWalk(git.getRepository())) {
      List<Ref> tagList;

      if (revision != null) {
        tagList = git.tagList().setContains(GitUtil.getRevisionId(git.getRepository(), revision)).call();
      } else {
        tagList = git.tagList().call();
      }

      return tagList.stream()
        .map(ref -> gitTagConverter.buildTag(git.getRepository(), revWalk, ref))
        .collect(toList());
    } catch (GitAPIException ex) {
      throw new InternalRepositoryException(repository, "could not read tags from repository", ex);
    }
  }

  public interface Factory {
    TagsCommand create(GitContext context);
  }

}
