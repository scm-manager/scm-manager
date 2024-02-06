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

package sonia.scm.web.lfs;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import sonia.scm.repository.Repository;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;

/**
 * Creates {@link BlobStore} objects to store lfs objects.
 *
 * @since 1.54
 */
@Singleton
public class LfsBlobStoreFactory {

  private static final String GIT_LFS_STORE_NAME = "git-lfs";

  private final BlobStoreFactory blobStoreFactory;

  @Inject
  public LfsBlobStoreFactory(BlobStoreFactory blobStoreFactory) {
    this.blobStoreFactory = blobStoreFactory;
  }

  /**
   * Provides a {@link BlobStore} corresponding to the SCM Repository.
   *
   * @param repository The SCM Repository to provide a LFS {@link BlobStore} for.
   *
   * @return blob store for the corresponding scm repository
   */
  public BlobStore getLfsBlobStore(Repository repository) {
    return blobStoreFactory
        .withName(GIT_LFS_STORE_NAME)
        .forRepository(repository)
        .build();
  }
}
