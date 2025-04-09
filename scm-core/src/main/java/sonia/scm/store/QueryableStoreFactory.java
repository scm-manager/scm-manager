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

/**
 * Factory to create {@link QueryableStore} and {@link QueryableMutableStore} instances.
 * In comparison to the {@link DataStoreFactory}, this factory is used to create stores which can execute
 * queries on the stored data. Queryable stores can be used for types which are annotated with {@link QueryableType}.
 * <br/>
 * Normally, there should be no need to use this factory directly. Instead, for each type annotated with
 * {@link QueryableType} a dedicated store factory is generated which can be injected into other components.
 * For instance, if your data class is named {@code MyData} and annotated with {@link QueryableType}, a factory
 * you should find a {@code MyDataStoreFactory} for your needs which is backed by this class.
 * <br/>
 * Implementations probably are backed by a database or a similar storage system instead of the familiar
 * file based storage using XML.
 *
 * @since 3.8.0
 */
public interface QueryableStoreFactory {

  /**
   * Creates a read-only store for the given class and optional parent ids. If parent ids are omitted, queries
   * will not be restricted to a specific parent (for example a repository) but will run on all data of the given type.
   *
   * @param clazz     The class of the data type (must be annotated with {@link QueryableType}).
   * @param parentIds Optional parent ids to restrict the query to a specific parent.
   * @param <T>       The type of the data.
   * @return A read-only store for the given class and optional parent ids.
   */
  <T> QueryableStore<T> getReadOnly(Class<T> clazz, String... parentIds);

  /**
   * Creates a mutable store for the given class and parent ids. In contrast to the read-only store, for a mutable store
   * the parent ids are mandatory. For each parent class given in the {@link QueryableType} annotation of the type, a
   * concrete id has to be specified. This is because mutable stores are used to store data, which is done for the
   * concrete parents only. So if data should be stored for different parents, separate mutable stores have to be
   * created.
   * <br/>
   * The mutable store provides methods to store, update and delete data but also all query methods of the read-only
   * store.
   *
   * @param clazz     The class of the data type (must be annotated with {@link QueryableType}).
   * @param parentIds Ids for all parent classes named in the {@link QueryableType} annotation.
   * @param <T>       The type of the data.
   * @return A mutable store for the given class scoped to the given parents.
   */
  <T> QueryableMutableStore<T> getMutable(Class<T> clazz, String... parentIds);

  <T> QueryableMaintenanceStore<T> getForMaintenance(Class<T> clazz, String... parentIds);

}
