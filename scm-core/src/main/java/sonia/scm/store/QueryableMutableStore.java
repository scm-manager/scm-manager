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
 * @since 3.7.0
 */
public interface QueryableMutableStore<T> extends DataStore<T>, QueryableStore<T>, AutoCloseable {
  void transactional(BooleanSupplier callback);

  @Override
  void close();
}
