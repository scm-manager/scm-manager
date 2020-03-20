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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.update.PropertyFileAccess;
import sonia.scm.util.IOUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class JAXBPropertyFileAccess implements PropertyFileAccess {

  private static final Logger LOG = LoggerFactory.getLogger(JAXBPropertyFileAccess.class);

  private final SCMContextProvider contextProvider;
  private final RepositoryLocationResolver locationResolver;

  @Inject
  public JAXBPropertyFileAccess(SCMContextProvider contextProvider, RepositoryLocationResolver locationResolver) {
    this.contextProvider = contextProvider;
    this.locationResolver = locationResolver;
  }

  @Override
  public Target renameGlobalConfigurationFrom(String oldName) {
    return newName -> {
      Path configDir = contextProvider.getBaseDirectory().toPath().resolve(StoreConstants.CONFIG_DIRECTORY_NAME);
      Path oldConfigFile = configDir.resolve(oldName + StoreConstants.FILE_EXTENSION);
      Path newConfigFile = configDir.resolve(newName + StoreConstants.FILE_EXTENSION);
      Files.move(oldConfigFile, newConfigFile);
    };
  }

  @Override
  public StoreFileTools forStoreName(String storeName) {
    return new StoreFileTools() {
      @Override
      public void forStoreFiles(FileConsumer storeFileConsumer) throws IOException {
        Path v1storeDir = computeV1StoreDir();
        if (Files.exists(v1storeDir) && Files.isDirectory(v1storeDir)) {
          try (Stream<Path> fileStream = Files.list(v1storeDir)) {
            fileStream.filter(p -> p.toString().endsWith(StoreConstants.FILE_EXTENSION)).forEach(p -> {
              try {
                String storeName = extractStoreName(p);
                storeFileConsumer.accept(p, storeName);
              } catch (IOException e) {
                throw new RuntimeException("could not call consumer for store file " + p + " with name " + storeName, e);
              }
            });
          }
        }
      }

      @Override
      public void moveAsRepositoryStore(Path storeFile, String repositoryId) throws IOException {
        Path repositoryLocation;
        try {
          repositoryLocation = locationResolver
            .forClass(Path.class)
            .getLocation(repositoryId);
        } catch (IllegalStateException e) {
          LOG.info("ignoring store file {} because there is no repository location for repository id {}", storeFile, repositoryId);
          return;
        }
        Path target = repositoryLocation
          .resolve(Store.DATA.getRepositoryStoreDirectory())
          .resolve(storeName);
        IOUtil.mkdirs(target.toFile());
        Path resolvedSourceFile = computeV1StoreDir().resolve(storeFile);
        Path resolvedTargetFile = target.resolve(storeFile.getFileName());
        LOG.trace("moving file {} to {}", resolvedSourceFile, resolvedTargetFile);
        Files.move(resolvedSourceFile, resolvedTargetFile);
      }

      private Path computeV1StoreDir() {
        return contextProvider.getBaseDirectory().toPath().resolve("var").resolve("data").resolve(storeName);
      }

      private String extractStoreName(Path p) {
        String fileName = p.getFileName().toString();
        return fileName.substring(0, fileName.length() - StoreConstants.FILE_EXTENSION.length());
      }
    };
  }
}
