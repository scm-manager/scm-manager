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


package sonia.scm.store;

import sonia.scm.repository.Repository;

/**
 * The BlobStoreFactory can be used to create a new or get an existing {@link BlobStore}s.
 * <br>
 * You can either create a global {@link BlobStore} or a {@link BlobStore} for a specific repository. To create a global
 * {@link BlobStore} call:
 * <code><pre>
 *     blobStoreFactory
 *       .withName("name")
 *       .build();
 * </pre></code>
 * To create a {@link BlobStore} for a specific repository call:
 * <code><pre>
 *     blobStoreFactory
 *       .withName("name")
 *       .forRepository(repository)
 *       .build();
 * </pre></code>
 *
 * @author Sebastian Sdorra
 * @since 1.23
 * 
 * @apiviz.landmark
 * @apiviz.uses sonia.scm.store.BlobStore
 */
public interface BlobStoreFactory {

  /**
   * Creates a new or gets an existing {@link BlobStore}. Instead of calling this method you should use the floating API
   * from {@link #withName(String)}.
   *
   * @param storeParameters The parameters for the blob store.
   * @return A new or an existing {@link BlobStore} for the given parameters.
   */
  BlobStore getStore(final StoreParameters storeParameters);

  /**
   * Use this to create a new or get an existing {@link BlobStore} with a floating API.
   * @param name The name for the {@link BlobStore}.
   * @return Floating API to either specify a repository or directly build a global {@link BlobStore}.
   */
  default FloatingStoreParameters.Builder withName(String name) {
    return new FloatingStoreParameters(this).new Builder(name);
  }
}

final class FloatingStoreParameters implements StoreParameters {

  private String name;
  private String repositoryId;

  private final BlobStoreFactory factory;

  FloatingStoreParameters(BlobStoreFactory factory) {
    this.factory = factory;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getRepositoryId() {
    return repositoryId;
  }

  public class Builder {

    Builder(String name) {
      FloatingStoreParameters.this.name = name;
    }

    /**
     * Use this to create or get a {@link BlobStore} for a specific repository. This step is optional. If you want to
     * have a global {@link BlobStore}, omit this.
     * @param repository The optional repository for the {@link BlobStore}.
     * @return Floating API to finish the call.
     */
    public FloatingStoreParameters.Builder forRepository(Repository repository) {
      FloatingStoreParameters.this.repositoryId = repository.getId();
      return this;
    }

    /**
     * Use this to create or get a {@link BlobStore} for a specific repository. This step is optional. If you want to
     * have a global {@link BlobStore}, omit this.
     * @param repositoryId The id of the optional repository for the {@link BlobStore}.
     * @return Floating API to finish the call.
     */
    public FloatingStoreParameters.Builder forRepository(String repositoryId) {
      FloatingStoreParameters.this.repositoryId = repositoryId;
      return this;
    }

    /**
     * Creates or gets the {@link BlobStore} with the given name and (if specified) the given repository. If no
     * repository is given, the {@link BlobStore} will be global.
     */
    public BlobStore build(){
      return factory.getStore(FloatingStoreParameters.this);
    }
  }
}
