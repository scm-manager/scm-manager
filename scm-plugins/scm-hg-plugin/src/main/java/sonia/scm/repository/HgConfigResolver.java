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

package sonia.scm.repository;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import jakarta.inject.Inject;

import java.io.File;
import java.util.function.Function;

public class HgConfigResolver {

  private final HgRepositoryHandler repositoryHandler;
  private final Function<Repository, HgRepositoryConfig> repositoryConfigResolver;
  private final Function<Repository, File> directoryResolver;

  @Inject
  public HgConfigResolver(HgRepositoryHandler repositoryHandler, HgRepositoryConfigStore repositoryConfigStore) {
    this(
      repositoryHandler,
      repositoryConfigStore::of,
      repository -> repositoryHandler.getDirectory(repository.getId())
    );
  }

  @VisibleForTesting
  public HgConfigResolver(HgRepositoryHandler repositoryHandler) {
    this(
      repositoryHandler,
      repository -> repositoryHandler.getDirectory(repository.getId())
    );
  }

  @VisibleForTesting
  public HgConfigResolver(HgRepositoryHandler repositoryHandler, Function<Repository, File> directoryResolver) {
    this.repositoryHandler = repositoryHandler;
    this.repositoryConfigResolver = (repository -> new HgRepositoryConfig());
    this.directoryResolver = directoryResolver;
  }

  @VisibleForTesting
  public HgConfigResolver(HgRepositoryHandler repositoryHandler, Function<Repository, HgRepositoryConfig> repositoryConfigResolver, Function<Repository, File> directoryResolver) {
    this.repositoryHandler = repositoryHandler;
    this.repositoryConfigResolver = repositoryConfigResolver;
    this.directoryResolver = directoryResolver;
  }

  public boolean isConfigured() {
    return repositoryHandler.isConfigured();
  }

  public HgConfig resolve(Repository repository) {
    HgGlobalConfig globalConfig = repositoryHandler.getConfig();
    HgRepositoryConfig repositoryConfig = repositoryConfigResolver.apply(repository);
    return new HgConfig(
      globalConfig.getHgBinary(),
      MoreObjects.firstNonNull(repositoryConfig.getEncoding(), globalConfig.getEncoding()),
      globalConfig.isShowRevisionInId(),
      globalConfig.isEnableHttpPostArgs(),
      directoryResolver.apply(repository)
    );
  }
}
