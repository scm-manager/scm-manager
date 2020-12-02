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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Tag;
import sonia.scm.security.GPG;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 * @author Sebastian Sdorra
 */
public class GitTagsCommand extends AbstractGitCommand implements TagsCommand {

  private final GPG gpg;

  /**
   * Constructs ...
   *
   * @param context
   */
  @Inject
  public GitTagsCommand(GitContext context, GPG gpp) {
    super(context);
    this.gpg = gpp;
  }

  //~--- get methods ----------------------------------------------------------

  @Override
  public List<Tag> getTags() throws IOException {
    List<Tag> tags;

    RevWalk revWalk = null;

    try (Git git = new Git(open())) {
      revWalk = new RevWalk(git.getRepository());

      List<Ref> tagList = git.tagList().call();

      tags = Lists.transform(tagList,
        new TransformFunction(git.getRepository(), revWalk, gpg));
    } catch (GitAPIException ex) {
      throw new InternalRepositoryException(repository, "could not read tags from repository", ex);
    } finally {
      GitUtil.release(revWalk);
    }

    return tags;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   * @author Enter your name here...
   * @version Enter version here..., 12/07/06
   */
  private static class TransformFunction implements Function<Ref, Tag> {

    /**
     * the logger for TransformFuntion
     */
    private static final Logger logger =
      LoggerFactory.getLogger(TransformFunction.class);

    //~--- constructors -------------------------------------------------------

    /**
     * Constructs ...
     *  @param repository
     * @param revWalk
     */
    public TransformFunction(Repository repository,
                             RevWalk revWalk,
                             GPG gpg) {
      this.repository = repository;
      this.revWalk = revWalk;
      this.gpg = gpg;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     * @param ref
     * @return
     */
    @Override
    public Tag apply(Ref ref) {
      Tag tag = null;

      try {
        RevCommit revCommit = GitUtil.getCommit(repository, revWalk, ref);
        if (revCommit != null) {
          String name = GitUtil.getTagName(ref);
          tag = new Tag(name, revCommit.getId().name(), GitUtil.getTagTime(revWalk, ref.getObjectId()));
          RevObject revObject = revWalk.parseAny(ref.getObjectId());
          if (revObject.getType() == Constants.OBJ_TAG) {
            RevTag revTag = (RevTag) revObject;
            GitUtil.getTagSignature(revTag, gpg, revWalk)
              .ifPresent(tag::addSignature);
          }
        }
      } catch (IOException ex) {
        logger.error("could not get commit for tag", ex);
      }

      return tag;
    }

    //~--- fields -------------------------------------------------------------

    /**
     * Field description
     */
    private final org.eclipse.jgit.lib.Repository repository;

    /**
     * Field description
     */
    private final RevWalk revWalk;
    private final GPG gpg;
  }
}
