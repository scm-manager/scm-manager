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

package sonia.scm.user;

import com.github.legman.Subscribe;
import com.google.common.annotations.VisibleForTesting;
import sonia.scm.plugin.Extension;
import sonia.scm.search.HandlerEventIndexSyncer;
import sonia.scm.search.Id;
import sonia.scm.search.Index;
import sonia.scm.search.Indexer;
import sonia.scm.search.SearchEngine;

import javax.inject.Inject;
import javax.inject.Singleton;

@Extension
@Singleton
public class UserIndexer implements Indexer<User> {

  @VisibleForTesting
  static final int VERSION = 1;

  private final UserManager userManager;
  private final SearchEngine searchEngine;

  @Inject
  public UserIndexer(UserManager userManager, SearchEngine searchEngine) {
    this.userManager = userManager;
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

  @Subscribe(async = false)
  public void handleEvent(UserEvent event) {
    new HandlerEventIndexSyncer<>(this).handleEvent(event);
  }

  @Override
  public Updater<User> open() {
    return new UserIndexUpdater(userManager, searchEngine.forType(User.class).getOrCreate());
  }

  public static class UserIndexUpdater implements Updater<User> {

    private final UserManager userManager;
    private final Index<User> index;

    private UserIndexUpdater(UserManager userManager, Index<User> index) {
      this.userManager = userManager;
      this.index = index;
    }

    @Override
    public void store(User user) {
      index.store(Id.of(user), UserPermissions.read(user).asShiroString(), user);
    }

    @Override
    public void delete(User user) {
      index.delete().byType().byId(Id.of(user));
    }

    @Override
    public void reIndexAll() {
      index.delete().byType().all();
      for (User user : userManager.getAll()) {
        store(user);
      }
    }

    @Override
    public void close() {
      index.close();
    }
  }
}
