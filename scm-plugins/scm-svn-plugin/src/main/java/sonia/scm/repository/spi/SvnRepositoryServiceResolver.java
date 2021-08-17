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
import sonia.scm.net.GlobalProxyConfiguration;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.SvnRepositoryHandler;
import sonia.scm.repository.SvnWorkingCopyFactory;
import sonia.scm.repository.api.HookContextFactory;

import javax.net.ssl.TrustManager;

@Extension
public class SvnRepositoryServiceResolver implements RepositoryServiceResolver {

  private final SvnRepositoryHandler handler;
  private final SvnWorkingCopyFactory workingCopyFactory;
  private final HookContextFactory hookContextFactory;
  private final TrustManager trustManager;
  private final GlobalProxyConfiguration globalProxyConfiguration;

  @Inject
  public SvnRepositoryServiceResolver(SvnRepositoryHandler handler,
                                      SvnWorkingCopyFactory workingCopyFactory,
                                      HookContextFactory hookContextFactory,
                                      TrustManager trustManager,
                                      GlobalProxyConfiguration globalProxyConfiguration) {
    this.handler = handler;
    this.workingCopyFactory = workingCopyFactory;
    this.hookContextFactory = hookContextFactory;
    this.trustManager = trustManager;
    this.globalProxyConfiguration = globalProxyConfiguration;
  }

  @Override
  public SvnRepositoryServiceProvider resolve(Repository repository) {
    SvnRepositoryServiceProvider provider = null;

    if (SvnRepositoryHandler.TYPE_NAME.equalsIgnoreCase(repository.getType())) {
      provider = new SvnRepositoryServiceProvider(
        handler, repository, workingCopyFactory, hookContextFactory, trustManager, globalProxyConfiguration
      );
    }

    return provider;
  }
}
