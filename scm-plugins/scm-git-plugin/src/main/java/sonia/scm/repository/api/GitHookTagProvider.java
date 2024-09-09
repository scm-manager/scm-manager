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

package sonia.scm.repository.api;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.Tag;

import java.io.IOException;
import java.util.List;

import static sonia.scm.repository.GitUtil.getId;

/**
 * Git provider implementation of {@link HookTagProvider}.
 *
 * @since 1.50
 */
public class GitHookTagProvider implements HookTagProvider {

  private static final Logger LOG = LoggerFactory.getLogger(GitHookTagProvider.class);

  private final List<Tag> createdTags;
  private final List<Tag> deletedTags;

  public GitHookTagProvider(List<ReceiveCommand> commands, Repository repository) {
    ImmutableList.Builder<Tag> createdTagBuilder = ImmutableList.builder();
    ImmutableList.Builder<Tag> deletedTagBuilder = ImmutableList.builder();

    for (ReceiveCommand rc : commands) {
      String refName = rc.getRefName();
      String tag = GitUtil.getTagName(refName);

      if (Strings.isNullOrEmpty(tag)) {
        LOG.debug("received ref name {} is not a tag", refName);
      } else {
        try (RevWalk revWalk = createRevWalk(repository)) {
          if (isCreate(rc)) {
            createdTagBuilder.add(createTagFromNewId(revWalk, rc, tag));
          } else if (isDelete(rc)) {
            deletedTagBuilder.add(createTagFromOldId(revWalk, rc, tag));
          } else if (isUpdate(rc)) {
            createdTagBuilder.add(createTagFromNewId(revWalk, rc, tag));
            deletedTagBuilder.add(createTagFromOldId(revWalk, rc, tag));
          }
        } catch (IOException e) {
          LOG.error("Could not read tag time", e);
        }
      }
    }

    createdTags = createdTagBuilder.build();
    deletedTags = deletedTagBuilder.build();
  }

  private Tag createTagFromNewId(RevWalk revWalk, ReceiveCommand rc, String tag) throws IOException {
    ObjectId newId = rc.getNewId();
    return new Tag(tag, getId(unpeelTag(revWalk, newId)), GitUtil.getTagTime(revWalk, newId));
  }

  private Tag createTagFromOldId(RevWalk revWalk, ReceiveCommand rc, String tag) throws IOException {
    ObjectId oldId = rc.getOldId();
    return new Tag(tag, getId(unpeelTag(revWalk, oldId)), GitUtil.getTagTime(revWalk, oldId));
  }

  public ObjectId unpeelTag(RevWalk revWalk, ObjectId oldId) throws IOException {
    RevObject revObject = revWalk.parseAny(oldId);
    if (revObject instanceof RevTag) {
      return unpeelTag(revWalk, ((RevTag) revObject).getObject());
    } else {
      return revObject;
    }
  }

  private boolean isUpdate(ReceiveCommand rc) {
    return rc.getType() == ReceiveCommand.Type.UPDATE || rc.getType() == ReceiveCommand.Type.UPDATE_NONFASTFORWARD;
  }

  private boolean isDelete(ReceiveCommand rc) {
    return rc.getType() == ReceiveCommand.Type.DELETE;
  }

  private boolean isCreate(ReceiveCommand rc) {
    return rc.getType() == ReceiveCommand.Type.CREATE;
  }

  @Override
  public List<Tag> getCreatedTags() {
    return createdTags;
  }

  @Override
  public List<Tag> getDeletedTags() {
    return deletedTags;
  }

  @VisibleForTesting
  RevWalk createRevWalk(Repository repository) {
    return new RevWalk(repository);
  }
}
