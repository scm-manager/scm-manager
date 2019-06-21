package sonia.scm.update;

import java.io.IOException;
import java.nio.file.Path;

public interface PropertyFileAccess {
  Target renameGlobalConfigurationFrom(String oldName);

  interface Target {
    void to(String newName) throws IOException;
  }

  StoreFileTools forStoreName(String name);

  interface StoreFileTools {
    void forStoreFiles(FileConsumer storeFileConsumer) throws IOException;

    void moveAsRepositoryStore(Path storeFile, String repositoryId) throws IOException;
  }

  public interface FileConsumer {
    void accept(Path file, String repositoryId) throws IOException;
  }
}
