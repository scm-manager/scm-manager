/**
 * Copyright (c) 2014, Sebastian Sdorra
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
 * http://bitbucket.org/sdorra/scm-manager
 *
 */
package sonia.scm.repository;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.event.ScmEventBus;
import sonia.scm.plugin.Extension;

/**
 * Repository listener which handles git related repository events.
 * 
 * @author Sebastian Sdorra
 * @since 1.50
 */
@Extension
@EagerSingleton
public class GitRepositoryModifyListener {
  
  /**
   * the logger for GitRepositoryModifyListener
   */
  private static final Logger logger = LoggerFactory.getLogger(GitRepositoryModifyListener.class);
  
  /**
   * Receives {@link RepositoryModificationEvent} and fires a {@link ClearRepositoryCacheEvent} if
   * the default branch of a git repository was modified.
   * 
   * @param event repository modification event
   */
  @Subscribe
  public void handleEvent(RepositoryModificationEvent event){
    Repository repository = event.getItem();
    
    if ( isModifyEvent(event) && 
         isGitRepository(event.getItem()) && 
         hasDefaultBranchChanged(event.getItemBeforeModification(), repository)) 
    {
      logger.info("git default branch of repository {} has changed, sending clear cache event", repository.getId());
      sendClearRepositoryCacheEvent(repository);
    }
  }
  
  @VisibleForTesting
  protected void sendClearRepositoryCacheEvent(Repository repository) {
    ScmEventBus.getInstance().post(new ClearRepositoryCacheEvent(repository));    
  }
  
  private boolean isModifyEvent(RepositoryEvent event) {
    return event.getEventType() == HandlerEventType.MODIFY;
  }
  
  private boolean isGitRepository(Repository repository) {
    return GitRepositoryHandler.TYPE_NAME.equals(repository.getType());
  }
  
  private boolean hasDefaultBranchChanged(Repository old, Repository current) {
    return !Objects.equal(
      old.getProperty(GitConstants.PROPERTY_DEFAULT_BRANCH), 
      current.getProperty(GitConstants.PROPERTY_DEFAULT_BRANCH)
    );
  }
  
}
