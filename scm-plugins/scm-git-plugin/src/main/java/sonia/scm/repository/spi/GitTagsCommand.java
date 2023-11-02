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

import com.google.inject.assistedinject.Assisted;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevWalk;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Tag;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

//~--- JDK imports ------------------------------------------------------------

/**
 * @author Sebastian Sdorra
 */
public class GitTagsCommand extends AbstractGitCommand implements TagsCommand {

  private final GitTagConverter gitTagConverter;

  /**
   * Constructs ...
   *
   * @param context
   */
  @Inject
  public GitTagsCommand(@Assisted GitContext context, GitTagConverter gitTagConverter) {
    super(context);
    this.gitTagConverter = gitTagConverter;
  }

  //~--- get methods ----------------------------------------------------------

  @Override
  public List<Tag> getTags() throws IOException {
    try (Git git = new Git(open()); RevWalk revWalk = new RevWalk(git.getRepository())) {
      List<Ref> tagList = git.tagList().call();

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
