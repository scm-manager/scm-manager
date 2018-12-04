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
 * The DataStoreFactory can be used to create new or get existing {@link DataStore}s.
 * <br>
 * You can either create a global {@link DataStore} or a {@link DataStore} for a specific repository.
 * To create a global {@link DataStore} call:
 * <code><pre>
 *     dataStoreFactory
 *       .withType(PersistedType.class)
 *       .withName("name")
 *       .build();
 * </pre></code>
 * To create a {@link DataStore} for a specific repository call:
 * <code><pre>
 *     dataStoreFactory
 *       .withType(PersistedType.class)
 *       .withName("name")
 *       .forRepository(repository)
 *       .build();
 * </pre></code>
 *
 * @author Sebastian Sdorra
 * @since 1.23
 * 
 * @apiviz.landmark
 * @apiviz.uses sonia.scm.store.DataStore
 */
public interface DataStoreFactory {

  /**
   * Creates a new or gets an existing {@link DataStore}. Instead of calling this method you should use the
   * floating API from {@link #withType(Class)}.
   *
   * @param storeParameters The parameters for the {@link DataStore}.
   * @return A new or an existing {@link DataStore} for the given parameters.
   */
  <T> DataStore<T> getStore(final TypedStoreParameters<T> storeParameters);

  /**
   * Use this to create a new or get an existing {@link DataStore} with a floating API.
   * @param type The type for the {@link DataStore}.
   * @return Floating API to set the name and either specify a repository or directly build a global
   * {@link DataStore}.
   */
  default <T> TypedFloatingDataStoreParameters<T>.Builder withType(Class<T> type) {
    return new TypedFloatingDataStoreParameters<T>(this).new Builder(type);
  }
}

final class TypedFloatingDataStoreParameters<T> {

  private final TypedStoreParametersImpl<T> parameters = new TypedStoreParametersImpl<>();
  private final DataStoreFactory factory;

  TypedFloatingDataStoreParameters(DataStoreFactory factory) {
    this.factory = factory;
  }

  public class Builder {

    Builder(Class<T> type) {
      parameters.setType(type);
    }

    /**
     * Use this to set the name for the {@link DataStore}.
     * @param name The name for the {@link DataStore}.
     * @return Floating API to either specify a repository or directly build a global {@link DataStore}.
     */
    public OptionalRepositoryBuilder withName(String name) {
      parameters.setName(name);
      return new OptionalRepositoryBuilder();
    }
  }

  public class OptionalRepositoryBuilder {

    /**
     * Use this to create or get a {@link DataStore} for a specific repository. This step is optional. If you
     * want to have a global {@link DataStore}, omit this.
     * @param repository The optional repository for the {@link DataStore}.
     * @return Floating API to finish the call.
     */
    public OptionalRepositoryBuilder forRepository(Repository repository) {
      parameters.setRepository(repository);
      return this;
    }

    /**
     * Creates or gets the {@link DataStore} with the given name and (if specified) the given repository. If no
     * repository is given, the {@link DataStore} will be global.
     */
    public DataStore<T> build(){
      return factory.getStore(parameters);
    }
  }
}
