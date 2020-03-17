/**
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
