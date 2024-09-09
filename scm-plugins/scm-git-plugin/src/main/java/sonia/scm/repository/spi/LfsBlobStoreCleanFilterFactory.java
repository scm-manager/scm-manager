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

import org.eclipse.jgit.lib.Repository;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

class LfsBlobStoreCleanFilterFactory {

  private final LfsBlobStoreFactory blobStoreFactory;
  private final sonia.scm.repository.Repository repository;
  private final Path targetFile;

  LfsBlobStoreCleanFilterFactory(LfsBlobStoreFactory blobStoreFactory, sonia.scm.repository.Repository repository, Path targetFile) {
    this.blobStoreFactory = blobStoreFactory;
    this.repository = repository;
    this.targetFile = targetFile;
  }

  @SuppressWarnings("squid:S1172")
    // suppress unused parameter to keep the api compatible to jgit's FilterCommandFactory
  LfsBlobStoreCleanFilter createFilter(Repository db, InputStream in, OutputStream out) {
    return new LfsBlobStoreCleanFilter(in, out, blobStoreFactory.getLfsBlobStore(repository), targetFile);
  }
}
