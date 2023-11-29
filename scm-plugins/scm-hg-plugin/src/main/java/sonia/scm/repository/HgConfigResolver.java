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
