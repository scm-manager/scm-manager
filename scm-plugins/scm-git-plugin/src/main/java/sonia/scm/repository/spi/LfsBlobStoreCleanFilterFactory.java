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
