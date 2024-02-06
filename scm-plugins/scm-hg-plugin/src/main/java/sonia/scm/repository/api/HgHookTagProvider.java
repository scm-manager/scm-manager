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
