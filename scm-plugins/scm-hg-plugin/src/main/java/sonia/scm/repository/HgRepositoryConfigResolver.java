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
 *
 */

package sonia.scm.repository;

import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;
import java.io.File;
import java.util.function.Function;

public class HgRepositoryConfigResolver {

  private final HgRepositoryHandler repositoryHandler;
  private final Function<Repository, File> directoryResolver;

  @Inject
  public HgRepositoryConfigResolver(HgRepositoryHandler repositoryHandler) {
    this(
      repositoryHandler,
      (Repository repository) -> repositoryHandler.getDirectory(repository.getId())
    );
  }

  @VisibleForTesting
  public HgRepositoryConfigResolver(HgRepositoryHandler repositoryHandler, Function<Repository, File> directoryResolver) {
    this.repositoryHandler = repositoryHandler;
    this.directoryResolver = directoryResolver;
  }

  public boolean isConfigured() {
    return repositoryHandler.isConfigured();
  }

  public HgRepositoryConfig resolve(Repository repository) {
    HgGlobalConfig config = repositoryHandler.getConfig();
    return new HgRepositoryConfig(
      config.getHgBinary(),
      config.getEncoding(),
      config.isShowRevisionInId(),
      config.isEnableHttpPostArgs(),
      directoryResolver.apply(repository)
    );
  }
}
