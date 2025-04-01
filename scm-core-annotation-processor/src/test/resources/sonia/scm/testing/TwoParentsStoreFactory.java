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
import java.lang.String;
import sonia.scm.repository.Repository;
import sonia.scm.store.QueryableMutableStore;
import sonia.scm.store.QueryableStore;
import sonia.scm.store.QueryableStoreFactory;
import sonia.scm.user.User;

public class TwoParentsStoreFactory {

  private final QueryableStoreFactory storeFactory;

  @Inject
  TwoParentsStoreFactory(QueryableStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  public QueryableStore<TwoParents> getOverall() {
    return storeFactory.getReadOnly(TwoParents.class);
  }

  public QueryableMutableStore<TwoParents> getMutable(String repositoryId, String userId) {
    return storeFactory.getMutable(TwoParents.class, repositoryId, userId);
  }

  public QueryableMutableStore<TwoParents> getMutable(Repository repository, User user) {
    return storeFactory.getMutable(TwoParents.class, repository.getId(), user.getId());
  }

  public QueryableStore<TwoParents> getOverlapping(String repositoryId) {
    return storeFactory.getReadOnly(TwoParents.class, repositoryId);
  }

  public QueryableStore<TwoParents> getOverlapping(Repository repository) {
    return storeFactory.getReadOnly(TwoParents.class, repository.getId());
  }

  public QueryableStore<TwoParents> get(String repositoryId, String userId) {
    return storeFactory.getReadOnly(TwoParents.class, repositoryId, userId);
  }

  public QueryableStore<TwoParents> get(Repository repository, User user) {
    return storeFactory.getReadOnly(TwoParents.class, repository.getId(), user.getId());
  }
}
