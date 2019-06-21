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

public class JAXBPropertyFileAccess implements PropertyFileAccess {

  private static final Logger LOG = LoggerFactory.getLogger(JAXBPropertyFileAccess.class);

  public static final String XML_FILENAME_SUFFIX = ".xml";
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
      Path oldConfigFile = configDir.resolve(oldName + XML_FILENAME_SUFFIX);
      Path newConfigFile = configDir.resolve(oldName + XML_FILENAME_SUFFIX);
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
          Files.list(v1storeDir).filter(p -> p.toString().endsWith(XML_FILENAME_SUFFIX)).forEach(p -> {
            try {
              String storeName = extractStoreName(p);
              storeFileConsumer.accept(p, storeName);
            } catch (IOException e) {
              throw new RuntimeException("could not call consumer for store file " + p + " with name " + storeName, e);
            }
          });
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
        return fileName.substring(0, fileName.length() - XML_FILENAME_SUFFIX.length());
      }
    };
  }
}
