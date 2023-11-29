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
    
package sonia.scm.store;

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
