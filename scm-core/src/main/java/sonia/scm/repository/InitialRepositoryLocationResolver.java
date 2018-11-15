package sonia.scm.repository;

import sonia.scm.SCMContextProvider;
import sonia.scm.io.FileSystem;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

/**
 *
 * A Location Resolver for File based Repository Storage.
 *
 * WARNING: The Locations provided with this class may not be used from the plugins to store any plugin specific files.
 *
 * Please use the {@link sonia.scm.store.DataStoreFactory } and the {@link sonia.scm.store.DataStore} classes to store data
 * Please use the {@link sonia.scm.store.BlobStoreFactory } and the {@link sonia.scm.store.BlobStore} classes to store binary files
 * Please use the {@link sonia.scm.store.ConfigurationStoreFactory} and the {@link sonia.scm.store.ConfigurationStore} classes  to store configurations
 *
 * @author Mohamed Karray
 * @since 2.0.0
 */
public final class InitialRepositoryLocationResolver {

  private static final String REPOSITORIES_DIRECTORY = "repositories";
  public static final String REPOSITORIES_NATIVE_DIRECTORY = "data";
  private SCMContextProvider context;
  private FileSystem fileSystem;


  @Inject
  public InitialRepositoryLocationResolver(SCMContextProvider context, FileSystem fileSystem) {
    this.context = context;
    this.fileSystem = fileSystem;
  }

  public static File getNativeDirectory(File repositoriesDirectory, String repositoryId) {
    return new File(repositoriesDirectory, repositoryId
      .concat(File.separator)
      .concat(REPOSITORIES_NATIVE_DIRECTORY));
  }

  public File getBaseDirectory() {
    return new File(context.getBaseDirectory(), REPOSITORIES_DIRECTORY);
  }

  public File createDirectory(Repository repository) throws IOException {
    File initialRepoFolder = getDirectory(repository);
    fileSystem.create(initialRepoFolder);
    return initialRepoFolder;
  }

  public File getDirectory(Repository repository) {
    return new File(context.getBaseDirectory(), REPOSITORIES_DIRECTORY
      .concat(File.separator)
      .concat(repository.getId()));
  }
}
