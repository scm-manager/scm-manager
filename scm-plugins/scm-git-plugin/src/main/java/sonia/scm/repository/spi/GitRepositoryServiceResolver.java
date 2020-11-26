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

package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Injector;
import sonia.scm.event.ScmEventBus;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.security.GPG;

/**
 *
 * @author Sebastian Sdorra
 */
@Extension
public class GitRepositoryServiceResolver implements RepositoryServiceResolver {

  private final Injector injector;
  private final GitContextFactory contextFactory;
  private final GPG gpg;
  private final HookContextFactory hookContextFactory;
  private final ScmEventBus scmEventBus;

  @Inject
  public GitRepositoryServiceResolver(Injector injector, GitContextFactory contextFactory, GPG gpg, HookContextFactory hookContextFactory) {
    this.injector = injector;
    this.contextFactory = contextFactory;
    this.gpg = gpg;
    this.hookContextFactory = hookContextFactory;
    this.scmEventBus = ScmEventBus.getInstance();
  }

  @Override
  public GitRepositoryServiceProvider resolve(Repository repository) {
    if (GitRepositoryHandler.TYPE_NAME.equalsIgnoreCase(repository.getType())) {
      return new GitRepositoryServiceProvider(injector, contextFactory.create(repository), gpg, hookContextFactory, scmEventBus);
    }
    return null;
  }
}
