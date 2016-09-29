/**
 * *
 * Copyright (c) 2015, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * https://bitbucket.org/sdorra/scm-manager
 *
 */
package sonia.scm.repository.api;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Tag;
import sonia.scm.repository.spi.HookChangesetProvider;
import sonia.scm.repository.spi.HookChangesetRequest;
import sonia.scm.repository.spi.HookChangesetResponse;

/**
 * Mercurial tag provider implementation.
 *
 * @author Sebastian Sdorra
 * @since 1.50
 */
public class HgHookTagProvider implements HookTagProvider {

  private static final Logger logger = LoggerFactory.getLogger(HgHookTagProvider.class);
  
  private static final HookChangesetRequest REQUEST = new HookChangesetRequest();

  private final HookChangesetProvider changesetProvider;

  private List<Tag> createdTags;
  private final List<Tag> deletedTags = Collections.emptyList();

  /**
   * Constructs a new instance.
   *
   * @param changesetProvider changeset provider
   */
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
        tags.add(new Tag(tagName, c.getId()));
      }
    }
  }

}
