package sonia.scm.repository.spi;

import org.eclipse.jgit.lib.Repository;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public class LfsBlobStoreCleanFilterFactory {

  private final LfsBlobStoreFactory blobStoreFactory;
  private final sonia.scm.repository.Repository repository;
  private final Path targetFile;

  public LfsBlobStoreCleanFilterFactory(LfsBlobStoreFactory blobStoreFactory, sonia.scm.repository.Repository repository, Path targetFile) {
    this.blobStoreFactory = blobStoreFactory;
    this.repository = repository;
    this.targetFile = targetFile;
  }

  LfsBlobStoreCleanFilter createFilter(Repository db, InputStream in, OutputStream out) throws IOException {
    return new LfsBlobStoreCleanFilter(db, in, out, blobStoreFactory.getLfsBlobStore(repository), targetFile);
  }
}
