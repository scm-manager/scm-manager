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

package sonia.scm.user;

import com.github.legman.Subscribe;
import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.plugin.Extension;
import sonia.scm.search.HandlerEventIndexSyncer;
import sonia.scm.search.Id;
import sonia.scm.search.Index;
import sonia.scm.search.IndexLogStore;
import sonia.scm.search.Indexer;
import sonia.scm.search.SearchEngine;
import sonia.scm.search.SerializableIndexTask;

@Extension
@Singleton
public class UserIndexer implements Indexer<User> {

  @VisibleForTesting
  static final int VERSION = 1;

  private final SearchEngine searchEngine;

  @Inject
  public UserIndexer(SearchEngine searchEngine) {
    this.searchEngine = searchEngine;
  }

  @Override
  public Class<User> getType() {
    return User.class;
  }

  @Override
  public int getVersion() {
    return VERSION;
  }

  @Override
  public Class<? extends ReIndexAllTask<User>> getReIndexAllTask() {
    return ReIndexAll.class;
  }

  @Override
  public SerializableIndexTask<User> createStoreTask(User user) {
    return index -> store(index, user);
  }

  @Override
  public SerializableIndexTask<User> createDeleteTask(User item) {
    return index -> index.delete().byId(Id.of(User.class, item));
  }

  @Subscribe(async = false)
  public void handleEvent(UserEvent event) {
    new HandlerEventIndexSyncer<>(searchEngine, this).handleEvent(event);
  }

  private static void store(Index<User> index, User user) {
    index.store(Id.of(User.class, user), UserPermissions.read(user).asShiroString(), user);
  }

  public static class ReIndexAll extends ReIndexAllTask<User> {

    private final UserManager userManager;

    @Inject
    public ReIndexAll(IndexLogStore logStore, UserManager userManager) {
      super(logStore, User.class, VERSION);
      this.userManager = userManager;
    }

    @Override
    public void update(Index<User> index) {
      index.delete().all();
      for (User user : userManager.getAll()) {
        store(index, user);
      }
    }
  }
}
