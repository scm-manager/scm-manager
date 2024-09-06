/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.store;

import com.google.common.collect.ImmutableSet;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.Repository;

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
  @EqualsAndHashCode(exclude = {"classLoader", "adapters"})
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
