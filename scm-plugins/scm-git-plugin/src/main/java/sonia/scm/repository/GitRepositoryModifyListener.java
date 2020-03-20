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
    
package sonia.scm.repository;

import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.api.v2.resources.GitRepositoryConfigChangedEvent;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;

/**
 * Repository listener which handles git related repository events.
 *
 * @author Sebastian Sdorra
 * @since 1.50
 */
@Extension
@EagerSingleton
public class GitRepositoryModifyListener {

  private final GitHeadModifier headModifier;
  private final GitRepositoryConfigStoreProvider storeProvider;

  @Inject
  public GitRepositoryModifyListener(GitHeadModifier headModifier, GitRepositoryConfigStoreProvider storeProvider) {
    this.headModifier = headModifier;
    this.storeProvider = storeProvider;
  }

  /**
   * Receives {@link RepositoryModificationEvent} and fires a {@link ClearRepositoryCacheEvent} if
   * the default branch of a git repository was modified.
   *
   * @param event repository modification event
   */
  @Subscribe
  public void handleEvent(GitRepositoryConfigChangedEvent event){
    Repository repository = event.getRepository();

    String defaultBranch = storeProvider.get(repository).get().getDefaultBranch();
    if (defaultBranch != null) {
      headModifier.ensure(repository, defaultBranch);
    }
  }
}
