package sonia.scm.update;

import java.io.IOException;
import java.nio.file.Path;

public interface BlobDirectoryAccess {

  void forBlobDirectories(BlobDirectoryConsumer blobDirectoryConsumer) throws IOException;

  void moveToRepositoryBlobStore(Path blobDirectory, String newDirectoryName, String repositoryId) throws IOException;

  interface BlobDirectoryConsumer {
    void accept(Path directory) throws IOException;
  }
}
