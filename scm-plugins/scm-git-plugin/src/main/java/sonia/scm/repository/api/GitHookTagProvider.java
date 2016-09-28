/***
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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.Tag;

/**
 * Git provider implementation of {@link HookTagProvider}.
 * 
 * @since 1.50
 * @author Sebastian Sdorra
 */
public class GitHookTagProvider implements HookTagProvider {

  private static final Logger logger = LoggerFactory.getLogger(GitHookTagProvider.class);
  
  private final List<Tag> createdTags;
  private final List<Tag> deletedTags;

  /**
   * Constructs new instance.
   * 
   * @param commands received commands
   */
  public GitHookTagProvider(List<ReceiveCommand> commands) {
    ImmutableList.Builder<Tag> createdTagBuilder = ImmutableList.builder();
    ImmutableList.Builder<Tag> deletedTagBuilder = ImmutableList.builder();
    
    for ( ReceiveCommand rc : commands ){
      String refName = rc.getRefName();
      String tag = GitUtil.getTagName(refName);
      
      if (Strings.isNullOrEmpty(tag)){
        logger.debug("received ref name {} is not a tag", refName);
      } else if (rc.getType() == ReceiveCommand.Type.CREATE) {
        createdTagBuilder.add(new Tag(tag, GitUtil.getId(rc.getNewId())));
      } else if (rc.getType() == ReceiveCommand.Type.DELETE){
        deletedTagBuilder.add(new Tag(tag, GitUtil.getId(rc.getOldId())));
      }
    }
    
    createdTags = createdTagBuilder.build();
    deletedTags = deletedTagBuilder.build();
  }
  
  @Override
  public List<Tag> getCreatedTags() {
    return createdTags;
  }

  @Override
  public List<Tag> getDeletedTags() {
    return deletedTags;
  }

}
