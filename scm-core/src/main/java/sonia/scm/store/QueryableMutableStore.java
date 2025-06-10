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

import java.util.function.BooleanSupplier;

/**
 * This interface is used to store objects annotated with {@link QueryableType}.
 * It combines the functionality of a {@link DataStore} and a {@link QueryableStore}.
 * In contrast to the {@link QueryableStore}, instances are always scoped to a specific parent (if the type this store
 * is created for as parent types specified in its annotation).
 * It will be created by the {@link QueryableStoreFactory}.
 * <br/>
 * It is not meant to be instantiated by users of the API. Instead, use the query factory created by the annotation
 * processor for the annotated type.
 *
 * @param <T> The type of the objects to query.
 * @since 3.8.0
 */
public interface QueryableMutableStore<T> extends DataStore<T>, QueryableStore<T>, AutoCloseable {
  void transactional(BooleanSupplier callback);

  @Override
  MutableQuery<T, ?> query(Condition<T>... conditions);

  @Override
  void close();

  /**
   *
   * @param <T> "Type" &ndash; type of the objects to query
   * @param <S> "Self" &ndash; specification of the {@link MutableQuery}.
   */
  interface MutableQuery<T, S extends MutableQuery<T, S>> extends Query<T, T, S> {
    /**
     * Deletes all entries except the {@code keptElements} highest ones in terms of the provided order and query.
     * <br/><br/>
     * For example, calling {@code retain(4)} after a query
     * {@code store.query(CREATION_TIME.after(Instant.now().minus(5, DAYS)).orderBy(Order.DESC)} will remove every entry
     * except those that
     * <ul>
     *   <li>Match any conditions given by preceding queries (here: {@code store.query(...)} saying that only elements
     *   newer than five days may be kept), and</li>
     *   <li>Are among the 4 first ones in terms of the order given by the query result (here: a descending order with
     *   the newest being first).</li>
     * </ul>
     * This function is expected to only work in the realm of the {@link QueryableMutableStore}. For example, elements with
     * other parent ids in some implementations are supposed to remain unaffected.
     * <br/><br/>
     * <em>Note:</em> {@link #deleteAll()} is identical to {@code retain(0)}.
     * @param keptElements Quantity of entities to be retained.
     * @since 3.9.0
     */
    void retain(long keptElements);

    /**
     * Deletes all entries matching the given query conditions.
     * <br/><br/>
     * For example, calling {@code deleteAll()} after a query
     * {@code store.query(CREATION_TIME.before(Instant.now().minus(5, DAYS))} will remove every entry with a
     * {@code CREATION_TIME} property older than five days and keep those that don't match this condition (newer date).
     * <br/>
     * If no conditions have been selected beforehand, all entries in the realm of this {@link QueryableMutableStore}
     * instance will be removed. It does not affect the structure of the store, and new entries may be added afterwards.
     * <br/><br/>
     * This function is expected to only work in the realm of the {@link QueryableMutableStore}. For example, elements with
     * other parent ids are supposed to remain unaffected.
     * <br/><br/>
     * <em>Note:</em> Consider {@link #retain(long)} if you prefer to deliberately keep a given amounts of elements instead.
     * @since 3.9.0
     */
    void deleteAll();
  }
}
