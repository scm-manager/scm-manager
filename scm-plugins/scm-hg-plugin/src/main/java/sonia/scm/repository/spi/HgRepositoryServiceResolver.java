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

import com.google.inject.Inject;
import com.google.inject.Injector;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;

/**
 * @author Sebastian Sdorra
 */
@Extension
public class HgRepositoryServiceResolver implements RepositoryServiceResolver {

  private final Injector injector;
  private final HgCommandContextFactory commandContextFactory;

  @Inject
  public HgRepositoryServiceResolver(Injector injector, HgCommandContextFactory commandContextFactory) {
    this.injector = injector;
    this.commandContextFactory = commandContextFactory;
  }

  @Override
  public HgRepositoryServiceProvider resolve(Repository repository) {
    if (HgRepositoryHandler.TYPE_NAME.equalsIgnoreCase(repository.getType())) {
      return new HgRepositoryServiceProvider(injector, commandContextFactory.create(repository));
    }
    return null;
  }
}
