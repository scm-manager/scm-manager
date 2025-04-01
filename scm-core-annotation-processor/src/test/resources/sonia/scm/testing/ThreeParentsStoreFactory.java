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

import sonia.scm.group.Group;
import sonia.scm.repository.Repository;
import sonia.scm.store.QueryableMutableStore;
import sonia.scm.store.QueryableStore;
import sonia.scm.store.QueryableStoreFactory;
import sonia.scm.user.User;

public class ThreeParentsStoreFactory {

  private final QueryableStoreFactory storeFactory;

  @Inject
  ThreeParentsStoreFactory(QueryableStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  public QueryableStore<ThreeParents> getOverall() {
    return storeFactory.getReadOnly(ThreeParents.class);
  }

  public QueryableMutableStore<ThreeParents> getMutable(String repositoryId, String userId, String groupId) {
    return storeFactory.getMutable(ThreeParents.class, repositoryId, userId, groupId);
  }

  public QueryableMutableStore<ThreeParents> getMutable(Repository repository, User user, Group group) {
    return storeFactory.getMutable(ThreeParents.class, repository.getId(), user.getId(), group.getId());
  }

  public QueryableStore<ThreeParents> getOverlapping(String repositoryId) {
    return storeFactory.getReadOnly(ThreeParents.class, repositoryId);
  }

  public QueryableStore<ThreeParents> getOverlapping(Repository repository) {
    return storeFactory.getReadOnly(ThreeParents.class, repository.getId());
  }

  public QueryableStore<ThreeParents> getOverlapping(String repositoryId, String userId) {
    return storeFactory.getReadOnly(ThreeParents.class, repositoryId, userId);
  }

  public QueryableStore<ThreeParents> getOverlapping(Repository repository, User user) {
    return storeFactory.getReadOnly(ThreeParents.class, repository.getId(), user.getId());
  }

  public QueryableStore<ThreeParents> get(String repositoryId, String userId, String groupId) {
    return storeFactory.getReadOnly(ThreeParents.class, repositoryId, userId, groupId);
  }

  public QueryableStore<ThreeParents> get(Repository repository, User user, Group group) {
    return storeFactory.getReadOnly(ThreeParents.class, repository.getId(), user.getId(), group.getId());
  }
}
