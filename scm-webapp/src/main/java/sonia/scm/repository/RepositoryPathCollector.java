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
