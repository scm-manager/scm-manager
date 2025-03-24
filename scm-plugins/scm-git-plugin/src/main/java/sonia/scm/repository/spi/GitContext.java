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

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryProvider;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;


public class GitContext implements Closeable, RepositoryProvider
{
  private static final Logger logger =
    LoggerFactory.getLogger(GitContext.class);

  private final File directory;
  private final Repository repository;
  private final GitRepositoryConfigStoreProvider storeProvider;
  private final GitConfig config;

  private org.eclipse.jgit.lib.Repository gitRepository;

  public GitContext(File directory, Repository repository, GitRepositoryConfigStoreProvider storeProvider, GitConfig config)
  {
    this.directory = directory;
    this.repository = repository;
    this.storeProvider = storeProvider;
    this.config = config;
  }


   @Override
  public void close()
  {
    logger.trace("close git repository {}", directory);

    GitUtil.close(gitRepository);
    gitRepository = null;
  }


  public org.eclipse.jgit.lib.Repository open()
  {
    if (gitRepository == null)
    {
      logger.trace("open git repository {}", directory);

      try {
        gitRepository = GitUtil.open(directory);
      } catch (IOException e) {
        throw new InternalRepositoryException(repository, "could not open git repository", e);
      }
    }

    return gitRepository;
  }

  Repository getRepository() {
    return repository;
  }

  @Override
  public Repository get() {
    return getRepository();
  }

  File getDirectory() {
    return directory;
  }

  GitRepositoryConfig getConfig() {
    GitRepositoryConfig config = storeProvider.get(repository).get();
    if (config == null) {
      return new GitRepositoryConfig();
    } else {
      return config;
    }
  }

  void setConfig(GitRepositoryConfig newConfig) {
    storeProvider.get(repository).set(newConfig);
  }

  @VisibleForTesting
  void setGitRepository(org.eclipse.jgit.lib.Repository gitRepository) {
    this.gitRepository = gitRepository;
  }

  GitConfig getGlobalConfig() {
    return config;
  }

}
