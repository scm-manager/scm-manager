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
      } else if (isCreate(rc)) {
        createdTagBuilder.add(createTagFromNewId(rc, tag));
      } else if (isDelete(rc)){
        deletedTagBuilder.add(createTagFromOldId(rc, tag));
      } else if (isUpdate(rc)) {
        createdTagBuilder.add(createTagFromNewId(rc, tag));
        deletedTagBuilder.add(createTagFromOldId(rc, tag));
      }
    }
    
    createdTags = createdTagBuilder.build();
    deletedTags = deletedTagBuilder.build();
  }

  private Tag createTagFromNewId(ReceiveCommand rc, String tag) {
    return new Tag(tag, GitUtil.getId(rc.getNewId()));
  }

  private Tag createTagFromOldId(ReceiveCommand rc, String tag) {
    return new Tag(tag, GitUtil.getId(rc.getOldId()));
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

}
