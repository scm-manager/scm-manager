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

import jakarta.inject.Inject;
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
import sonia.scm.repository.Tag;
import sonia.scm.security.GPG;

import java.io.IOException;

class GitTagConverter {

  private static final Logger LOG = LoggerFactory.getLogger(GitTagConverter.class);

  private final GPG gpg;

  @Inject
  GitTagConverter(GPG gpg) {
    this.gpg = gpg;
  }

  public Tag buildTag(RevTag revTag, RevWalk revWalk) {
    Tag tag = null;
    try {
      RevCommit revCommit = revWalk.parseCommit(revTag.getObject().getId());
      tag = new Tag(revTag.getTagName(), revCommit.getId().name(), revTag.getTaggerIdent().getWhen().getTime());
      GitUtil.getTagSignature(revTag, gpg, revWalk).ifPresent(tag::addSignature);
    } catch (IOException ex) {
      LOG.error("could not get commit for tag", ex);
    }
    return tag;
  }

  public Tag buildTag(Repository repository, RevWalk revWalk, Ref ref) {
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
      LOG.error("could not get commit for tag", ex);
    }

    return tag;
  }

}
