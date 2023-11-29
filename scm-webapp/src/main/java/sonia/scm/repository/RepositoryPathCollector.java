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

import jakarta.inject.Inject;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

@Extension
public class RepositoryPathCollector {

  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public RepositoryPathCollector(RepositoryServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  public RepositoryPaths collect(NamespaceAndName repository, String revision) throws IOException {
    BrowserResult result = browse(repository, revision);
    Collection<String> paths = new HashSet<>();
    append(paths, result.getFile());
    return new RepositoryPaths(result.getRevision(), paths);
  }

  private BrowserResult browse(NamespaceAndName repository, String revision) throws IOException {
    try (RepositoryService repositoryService = serviceFactory.create(repository)) {
      return repositoryService.getBrowseCommand()
        .setDisableSubRepositoryDetection(true)
        .setDisableLastCommit(true)
        .setDisablePreProcessors(true)
        .setLimit(Integer.MAX_VALUE)
        .setRecursive(true)
        .setRevision(revision)
        .getBrowserResult();
    }
  }

  private void append(Collection<String> paths, FileObject file) {
    if (file.isDirectory()) {
      for (FileObject child : file.getChildren()) {
        append(paths, child);
      }
    } else {
      paths.add(file.getPath());
    }
  }

}
