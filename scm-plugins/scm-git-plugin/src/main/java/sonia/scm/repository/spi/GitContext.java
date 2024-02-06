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

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.GitUtil;
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


  public org.eclipse.jgit.lib.Repository open() throws IOException
  {
    if (gitRepository == null)
    {
      logger.trace("open git repository {}", directory);

      gitRepository = GitUtil.open(directory);
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
