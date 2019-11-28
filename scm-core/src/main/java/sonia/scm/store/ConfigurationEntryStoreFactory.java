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
 * The ConfigurationEntryStoreFactory can be used to create new or get existing {@link ConfigurationEntryStore}s.
 * <br>
 * <b>Note:</b> the default implementation uses the same location as the {@link ConfigurationStoreFactory}, so be sure
 * that the store names are unique for all {@link ConfigurationEntryStore}s and {@link ConfigurationEntryStore}s.
 * <br>
 * You can either create a global {@link ConfigurationEntryStore} or a {@link ConfigurationEntryStore} for a specific
 * repository. To create a global {@link ConfigurationEntryStore} call:
 * <code><pre>
 *     configurationEntryStoreFactory
 *       .withType(PersistedType.class)
 *       .withName("name")
 *       .build();
 * </pre></code>
 * To create a {@link ConfigurationEntryStore} for a specific repository call:
 * <code><pre>
 *     configurationEntryStoreFactory
 *       .withType(PersistedType.class)
 *       .withName("name")
 *       .forRepository(repository)
 *       .build();
 * </pre></code>
 *
 * @author Sebastian Sdorra
 * @since 1.31
 * 
 * @apiviz.landmark
 * @apiviz.uses sonia.scm.store.ConfigurationEntryStore
 */
public interface ConfigurationEntryStoreFactory {

  /**
   * Creates a new or gets an existing {@link ConfigurationEntryStore}. Instead of calling this method you should use
   * the floating API from {@link #withType(Class)}.
   *
   * @param storeParameters The parameters for the {@link ConfigurationEntryStore}.
   * @return A new or an existing {@link ConfigurationEntryStore} for the given parameters.
   */
  <T> ConfigurationEntryStore<T> getStore(final TypedStoreParameters<T> storeParameters);

  /**
   * Use this to create a new or get an existing {@link ConfigurationEntryStore} with a floating API.
   * @param type The type for the {@link ConfigurationEntryStore}.
   * @return Floating API to set the name and either specify a repository or directly build a global
   * {@link ConfigurationEntryStore}.
   */
  default <T> TypedFloatingConfigurationEntryStoreParameters<T>.Builder withType(Class<T> type) {
    return new TypedFloatingConfigurationEntryStoreParameters<T>(this).new Builder(type);
  }
}

final class TypedFloatingConfigurationEntryStoreParameters<T> {

  private final TypedStoreParametersImpl<T> parameters = new TypedStoreParametersImpl<>();
  private final ConfigurationEntryStoreFactory factory;

  TypedFloatingConfigurationEntryStoreParameters(ConfigurationEntryStoreFactory factory) {
    this.factory = factory;
  }

  public class Builder {

    Builder(Class<T> type) {
      parameters.setType(type);
    }

    /**
     * Use this to set the name for the {@link ConfigurationEntryStore}.
     * @param name The name for the {@link ConfigurationEntryStore}.
     * @return Floating API to either specify a repository or directly build a global {@link ConfigurationEntryStore}.
     */
    public OptionalRepositoryBuilder withName(String name) {
      parameters.setName(name);
      return new OptionalRepositoryBuilder();
    }
  }

  public class OptionalRepositoryBuilder {

    /**
     * Use this to create or get a {@link ConfigurationEntryStore} for a specific repository. This step is optional. If
     * you want to have a global {@link ConfigurationEntryStore}, omit this.
     * @param repository The optional repository for the {@link ConfigurationEntryStore}.
     * @return Floating API to finish the call.
     */
    public OptionalRepositoryBuilder forRepository(Repository repository) {
      parameters.setRepositoryId(repository.getId());
      return this;
    }

    /**
     * Use this to create or get a {@link ConfigurationEntryStore} for a specific repository. This step is optional. If
     * you want to have a global {@link ConfigurationEntryStore}, omit this.
     * @param repositoryId The id of the optional repository for the {@link ConfigurationEntryStore}.
     * @return Floating API to finish the call.
     */
    public OptionalRepositoryBuilder forRepository(String repositoryId) {
      parameters.setRepositoryId(repositoryId);
      return this;
    }

    /**
     * Creates or gets the {@link ConfigurationEntryStore} with the given name and (if specified) the given repository.
     * If no repository is given, the {@link ConfigurationEntryStore} will be global.
     */
    public ConfigurationEntryStore<T> build(){
      return factory.getStore(parameters);
    }
  }
}
