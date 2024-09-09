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
