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

package sonia.scm.testing;

import jakarta.inject.Inject;
import javax.annotation.processing.Generated;
import sonia.scm.store.QueryableMutableStore;
import sonia.scm.store.QueryableStore;
import sonia.scm.store.QueryableStoreFactory;

@Generated("sonia.scm.annotation.QueryableTypeAnnotationProcessor")
public class AStoreFactory {

  private final QueryableStoreFactory storeFactory;

  @Inject
  AStoreFactory(QueryableStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  public QueryableStore<A> get() {
    return storeFactory.getReadOnly(A.class);
  }

  public QueryableMutableStore<A> getMutable() {
    return storeFactory.getMutable(A.class);
  }
}
