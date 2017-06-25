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

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.HandlerEventType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;

/**
 * Unit tests for {@link LfsStoreRemoveListener}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class LfsStoreRemoveListenerTest {
  
  @Mock
  private LfsBlobStoreFactory lfsBlobStoreFactory;
  
  @Mock
  private BlobStore blobStore;
  
  @InjectMocks
  private LfsStoreRemoveListener lfsStoreRemoveListener;
  
  @Test
  public void testHandleRepositoryEventWithNonDeleteEvents() {
    lfsStoreRemoveListener.handleRepositoryEvent(event(HandlerEventType.BEFORE_CREATE));
    lfsStoreRemoveListener.handleRepositoryEvent(event(HandlerEventType.CREATE));
    
    lfsStoreRemoveListener.handleRepositoryEvent(event(HandlerEventType.BEFORE_MODIFY));
    lfsStoreRemoveListener.handleRepositoryEvent(event(HandlerEventType.MODIFY));
    
    lfsStoreRemoveListener.handleRepositoryEvent(event(HandlerEventType.BEFORE_DELETE));
    
    verifyZeroInteractions(lfsBlobStoreFactory);
  }
  
  @Test
  public void testHandleRepositoryEventWithNonGitRepositories() {
    lfsStoreRemoveListener.handleRepositoryEvent(event(HandlerEventType.DELETE, "svn"));
    lfsStoreRemoveListener.handleRepositoryEvent(event(HandlerEventType.DELETE, "hg"));
    lfsStoreRemoveListener.handleRepositoryEvent(event(HandlerEventType.DELETE, "dummy"));
    
    verifyZeroInteractions(lfsBlobStoreFactory);
  }
  
  @Test
  public void testHandleRepositoryEvent() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold("git");
    
    when(lfsBlobStoreFactory.getLfsBlobStore(heartOfGold)).thenReturn(blobStore);
    Blob blobA = mockBlob("a");
    Blob blobB = mockBlob("b");
    List<Blob> blobs = Lists.newArrayList(blobA, blobB);
    when(blobStore.getAll()).thenReturn(blobs);
    
    
    lfsStoreRemoveListener.handleRepositoryEvent(new RepositoryEvent(HandlerEventType.DELETE, heartOfGold));
    verify(blobStore).getAll();
    verify(blobStore).remove(blobA);
    verify(blobStore).remove(blobB);
    
    verifyNoMoreInteractions(blobStore);
  }
  
  private Blob mockBlob(String id) {
    Blob blob = mock(Blob.class);
    when(blob.getId()).thenReturn(id);
    return blob;
  }

  private RepositoryEvent event(HandlerEventType eventType) {
    return event(eventType, "git");
  }
  
  private RepositoryEvent event(HandlerEventType eventType, String repositoryType) {
    return new RepositoryEvent(eventType, RepositoryTestData.create42Puzzle(repositoryType));
  }
  
}
