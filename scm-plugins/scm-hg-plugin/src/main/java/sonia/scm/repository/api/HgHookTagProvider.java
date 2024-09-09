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

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Tag;
import sonia.scm.repository.spi.HookChangesetRequest;
import sonia.scm.repository.spi.HookChangesetResponse;

/**
 * Mercurial tag provider implementation.
 *
 * @since 1.50
 */
public class HgHookTagProvider implements HookTagProvider {

  private static final Logger logger = LoggerFactory.getLogger(HgHookTagProvider.class);

  private static final HookChangesetRequest REQUEST = new HookChangesetRequest();

  private final HookChangesetProvider changesetProvider;

  private List<Tag> createdTags;
  private final List<Tag> deletedTags = Collections.emptyList();

  public HgHookTagProvider(HookChangesetProvider changesetProvider) {
    this.changesetProvider = changesetProvider;
  }

  @Override
  public List<Tag> getCreatedTags() {
    if (createdTags == null) {
      collect();
    }
    return createdTags;
  }

  @Override
  public List<Tag> getDeletedTags() {
    logger.warn("detecting deleted tags with mercurial is currently not supported");
    return deletedTags;
  }

  private void collect() {
    ImmutableList.Builder<Tag> createdTagsBuilder = ImmutableList.builder();

    logger.trace("collecting tags from hook changesets");
    HookChangesetResponse response = changesetProvider.handleRequest(REQUEST);
    for ( Changeset c : response.getChangesets() ){
      appendTags(createdTagsBuilder, c);
    }

    createdTags = createdTagsBuilder.build();
  }

  private void appendTags(ImmutableList.Builder<Tag> tags, Changeset c){
    List<String> tagNames = c.getTags();
    if (tagNames != null){
      for ( String tagName : tagNames ){
        logger.trace("found tag {} at changeset {}", tagName, c.getId());
        tags.add(new Tag(tagName, c.getId(), c.getDate()));
      }
    }
  }

}
