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
