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

package sonia.scm.repository.client.api;


import com.google.common.collect.Lists;

import sonia.scm.repository.client.spi.RepositoryClientFactoryProvider;
import sonia.scm.util.ServiceUtil;

import java.io.File;
import java.io.IOException;

import java.util.List;

/**
 * @since 1.18
 */
public final class RepositoryClientFactory {

  private Iterable<RepositoryClientFactoryProvider> providers;

  public RepositoryClientFactory() {
    this.providers =
      ServiceUtil.getServices(RepositoryClientFactoryProvider.class);
  }

  public RepositoryClientFactory(
    Iterable<RepositoryClientFactoryProvider> providers) {
    this.providers = providers;
  }

  public RepositoryClient create(String type, File main, File workingCopy)
    throws IOException {

    return new RepositoryClient(getProvider(type).create(main, workingCopy));
  }

  public RepositoryClient create(String type, String url, String username,
                                 String password, File workingCopy)
    throws IOException {
    return new RepositoryClient(getProvider(type).create(url, username,
      password, workingCopy));
  }

  public RepositoryClient create(String type, String url, File workingCopy)
    throws IOException {
    return new RepositoryClient(getProvider(type).create(url, null, null, workingCopy));
  }

  public Iterable<String> getAvailableTypes() {
    List<String> types = Lists.newArrayList();

    for (RepositoryClientFactoryProvider provider : providers) {
      types.add(provider.getType());
    }

    return types;
  }

  private RepositoryClientFactoryProvider getProvider(String type) {
    RepositoryClientFactoryProvider provider = null;

    for (RepositoryClientFactoryProvider p : providers) {
      if (p.getType().equalsIgnoreCase(type)) {
        provider = p;

        break;
      }
    }

    if (provider == null) {
      throw new RuntimeException(
        "could not find provider for type ".concat(type));
    }

    return provider;
  }
}
