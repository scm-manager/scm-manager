/**
 * Copyright (c) 2010, Sebastian Sdorra
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


package sonia.scm.web.lfs;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;

/**
 * Listener which removes all lfs objects from a blob store, whenever its corresponding git repository gets deleted.
 * 
 * @author Sebastian Sdorra
 * @since 1.54
 */
@Extension
@EagerSingleton
public class LfsStoreRemoveListener {
  
  private static final Logger LOG = LoggerFactory.getLogger(LfsBlobStoreFactory.class);
  
  private final LfsBlobStoreFactory lfsBlobStoreFactory;

  @Inject
  public LfsStoreRemoveListener(LfsBlobStoreFactory lfsBlobStoreFactory) {
    this.lfsBlobStoreFactory = lfsBlobStoreFactory;
  }
  
  /**
   * Remove all object from the blob store, if the event is an delete event and the repository is a git repository.
   * 
   * @param event repository event
   */
  @Subscribe
  public void handleRepositoryEvent(RepositoryEvent event) {
    if ( isDeleteEvent(event) && isGitRepositoryEvent(event) ) {
      removeLfsStore(event.getItem());
    }
  }
  
  private boolean isDeleteEvent(RepositoryEvent event) {
    return HandlerEventType.DELETE == event.getEventType();
  }
  
  private boolean isGitRepositoryEvent(RepositoryEvent event) {
    return event.getItem() != null 
        && event.getItem().getType().equals(GitRepositoryHandler.TYPE_NAME);
  }
  
  private void removeLfsStore(Repository repository) {
    LOG.debug("remove all blobs from store, because corresponding git repository {} was removed", repository.getName());
    BlobStore blobStore = lfsBlobStoreFactory.getLfsBlobStore(repository);
    for ( Blob blob : blobStore.getAll() ) {
      LOG.trace("remove blob {}, because repository {} was removed", blob.getId(), repository.getName());
      blobStore.remove(blob);
    }
  }
  
}
