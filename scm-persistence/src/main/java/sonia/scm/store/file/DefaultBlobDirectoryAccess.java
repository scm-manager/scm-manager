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

package sonia.scm.store.file;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.update.BlobDirectoryAccess;
import sonia.scm.util.IOUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class DefaultBlobDirectoryAccess implements BlobDirectoryAccess {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultBlobDirectoryAccess.class);

  private final SCMContextProvider contextProvider;
  private final RepositoryLocationResolver locationResolver;

  @Inject
  public DefaultBlobDirectoryAccess(SCMContextProvider contextProvider, RepositoryLocationResolver locationResolver) {
    this.contextProvider = contextProvider;
    this.locationResolver = locationResolver;
  }

  @Override
  public void forBlobDirectories(BlobDirectoryConsumer blobDirectoryConsumer) throws IOException {
    Path v1blobDir = computeV1BlobDir();
    if (Files.exists(v1blobDir) && Files.isDirectory(v1blobDir)) {
      try (Stream<Path> fileStream = Files.list(v1blobDir)) {
        fileStream.filter(p -> Files.isDirectory(p)).forEach(p -> {
          try {
            blobDirectoryConsumer.accept(p);
          } catch (IOException e) {
            throw new RuntimeException("could not call consumer for blob directory " + p, e);
          }
        });
      }
    }
  }

  @Override
  public void moveToRepositoryBlobStore(Path blobDirectory, String newDirectoryName, String repositoryId) throws IOException {
    Path repositoryLocation;
    try {
      repositoryLocation = locationResolver
        .forClass(Path.class)
        .getLocation(repositoryId);
    } catch (IllegalStateException e) {
      LOG.info("ignoring blob directory {} because there is no repository location for repository id {}", blobDirectory, repositoryId);
      return;
    }
    Path target = repositoryLocation
      .resolve(Store.BLOB.getRepositoryStoreDirectory());
    IOUtil.mkdirs(target.toFile());
    Path resolvedSourceDirectory = computeV1BlobDir().resolve(blobDirectory);
    Path resolvedTargetDirectory = target.resolve(newDirectoryName);
    LOG.trace("moving directory {} to {}", resolvedSourceDirectory, resolvedTargetDirectory);
    Files.move(resolvedSourceDirectory, resolvedTargetDirectory);
  }

  private Path computeV1BlobDir() {
    return contextProvider.getBaseDirectory().toPath().resolve("var").resolve("blob");
  }
}
