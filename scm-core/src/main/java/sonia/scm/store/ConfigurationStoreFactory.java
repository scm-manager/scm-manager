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

package sonia.scm.store;


/**
 * The ConfigurationStoreFactory can be used to create new or get existing {@link ConfigurationStore} objects.
 * <br>
 * <b>Note:</b> the default implementation uses the same location as the {@link ConfigurationEntryStoreFactory}, so be
 * sure that the store names are unique for all {@link ConfigurationEntryStore}s and {@link ConfigurationStore}s.
 * <br>
 * You can either create a global {@link ConfigurationStore} or a {@link ConfigurationStore} for a specific repository.
 * To create a global {@link ConfigurationStore} call:
 * <code><pre>
 *     configurationStoreFactory
 *       .withType(PersistedType.class)
 *       .withName("name")
 *       .build();
 * </pre></code>
 * To create a {@link ConfigurationStore} for a specific repository call:
 * <code><pre>
 *     configurationStoreFactory
 *       .withType(PersistedType.class)
 *       .withName("name")
 *       .forRepository(repository)
 *       .build();
 * </pre></code>
 *
 *
 * @apiviz.landmark
 * @apiviz.uses sonia.scm.store.ConfigurationStore
 */
public interface ConfigurationStoreFactory  {

  /**
   * Creates a new or gets an existing {@link ConfigurationStore}. Instead of calling this method you should use the
   * floating API from {@link #withType(Class)}.
   *
   * @param storeParameters The parameters for the {@link ConfigurationStore}.
   * @return A new or an existing {@link ConfigurationStore} for the given parameters.
   */
  <T> ConfigurationStore<T> getStore(final TypedStoreParameters<T> storeParameters);

  /**
   * Use this to create a new or get an existing {@link ConfigurationStore} with a floating API.
   * @param type The type for the {@link ConfigurationStore}.
   * @return Floating API to set the name and either specify a repository or directly build a global
   * {@link ConfigurationStore}.
   */
  default <T> TypedStoreParametersBuilder<T,ConfigurationStore<T>> withType(Class<T> type) {
    return new TypedStoreParametersBuilder<>(type, this::getStore);
  }
}
