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

import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.Repository;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Builder for {@link TypedStoreParameters}.
 *
 * @param <T> type of data
 * @param <S> type of store
 */
public final class TypedStoreParametersBuilder<T,S> {

  private final TypedStoreParametersImpl<T> parameters;
  private final Function<TypedStoreParameters<T>, S> factory;

  TypedStoreParametersBuilder(Class<T> type, Function<TypedStoreParameters<T>, S> factory) {
    this.factory = factory;
    this.parameters = new TypedStoreParametersImpl<>(type);
  }

  /**
   * Use this to set the name for the store.
   * @param name The name for the store.
   * @return Floating API to either specify a repository or directly build a global store.
   */
  public OptionalRepositoryBuilder withName(String name) {
    parameters.setName(name);
    return new OptionalRepositoryBuilder();
  }

  @Getter
  @Setter(AccessLevel.PRIVATE)
  @RequiredArgsConstructor
  private static class TypedStoreParametersImpl<T> implements TypedStoreParameters<T> {
    private final Class<T> type;
    private String name;
    private String repositoryId;
    private String namespace;
    private ClassLoader classLoader;
    private Set<XmlAdapter<?, ?>> adapters;

    public Optional<ClassLoader> getClassLoader() {
      return Optional.ofNullable(classLoader);
    }
  }

  public class OptionalRepositoryBuilder {

    private final Set<XmlAdapter<?,?>> adapters = new HashSet<>();

    /**
     * Use this to create or get a store for a specific repository. This step is optional. If you
     * want to have a global store, omit this.
     * @param repository The optional repository for the store.
     * @return Floating API to finish the call.
     */
    public OptionalRepositoryBuilder forRepository(Repository repository) {
      if (parameters.getNamespace() != null) {
        throw new IllegalStateException("Namespace for store already set. Cannot apply repository id for store.");
      }
      parameters.setRepositoryId(repository.getId());
      return this;
    }

    /**
     * Use this to create or get a store for a specific repository. This step is optional. If you
     * want to have a global store, omit this.
     * @param repositoryId The id of the optional repository for the store.
     * @return Floating API to finish the call.
     */
    public OptionalRepositoryBuilder forRepository(String repositoryId) {
      if (parameters.getNamespace() != null) {
        throw new IllegalStateException("Namespace for store already set. Cannot apply repository id for store.");
      }
      parameters.setRepositoryId(repositoryId);
      return this;
    }

    /**
     * Use this to create or get a store for a specific namespace. This step is optional. If you
     * want to have a global store, omit this.
     * @param namespace The name of the optional namespace for the store.
     * @return Floating API to finish the call.
     *
     * @since 2.44.0
     */
    public OptionalRepositoryBuilder forNamespace(String namespace) {
      if (parameters.getRepositoryId() != null) {
        throw new IllegalStateException("Repository id for store already set. Cannot apply namespace for store.");
      }
      parameters.setNamespace(namespace);
      return this;
    }

    /**
     * Sets the {@link ClassLoader} which is used as context class loader during marshaling and unmarshalling.
     * This is especially useful for storing class objects which come from an unknown source, in this case the
     * UberClassLoader ({@link PluginLoader#getUberClassLoader()} could be used for the store.
     *
     * @param classLoader classLoader for the context
     *
     * @return {@code this}
     */
    public OptionalRepositoryBuilder withClassLoader(ClassLoader classLoader) {
      parameters.setClassLoader(classLoader);
      return this;
    }

    /**
     * Sets an instance of an {@link XmlAdapter}.
     *
     * @param adapter adapter
     * @return {@code this}
     */
    public OptionalRepositoryBuilder withAdapter(XmlAdapter<?, ?> adapter) {
      adapters.add(adapter);
      return this;
    }

    /**
     * Creates or gets the store with the given name and (if specified) the given repository. If no
     * repository is given, the store will be global.
     */
    public S build(){
      parameters.setAdapters(ImmutableSet.copyOf(adapters));
      return factory.apply(parameters);
    }
  }

}
