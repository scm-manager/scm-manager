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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import sonia.scm.repository.Repository;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;

/**
 * Creates {@link BlobStore} objects to store lfs objects.
 * 
 * @author Sebastian Sdorra
 * @since 1.54
 */
@Singleton
public class LfsBlobStoreFactory {
  
  private static final String GIT_LFS_REPOSITORY_POSTFIX = "-git-lfs";
  
  private final BlobStoreFactory blobStoreFactory;

  /**
   * Create a new instance.
   * 
   * @param blobStoreFactory blob store factory
   */
  @Inject
  public LfsBlobStoreFactory(BlobStoreFactory blobStoreFactory) {
    this.blobStoreFactory = blobStoreFactory;
  }
  
  /**
   * Provides a {@link BlobStore} corresponding to the SCM Repository.
   * <p>
   * git-lfs repositories should generally carry the same name as their regular SCM repository counterparts. However,
   * we have decided to store them under their IDs instead of their names, since the names might change and provide
   * other drawbacks, as well.
   * <p>
   * These repositories will have {@linkplain #GIT_LFS_REPOSITORY_POSTFIX} appended to their IDs.
   *
   * @param repository The SCM Repository to provide a LFS {@link BlobStore} for.
   * 
   * @return blob store for the corresponding scm repository
   */
  public BlobStore getLfsBlobStore(Repository repository) {
    return blobStoreFactory.getBlobStore(repository.getId() + GIT_LFS_REPOSITORY_POSTFIX);
  }
}
