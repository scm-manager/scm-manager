package sonia.scm.repository;

import sonia.scm.SCMContextProvider;
import sonia.scm.io.FileSystem;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

/**
 * A Location Resolver for File based Repository Storage.
 * <p>
 * <b>WARNING:</b> The Locations provided with this class may not be used from the plugins to store any plugin specific files.
 * <p>
 * Please use the {@link sonia.scm.store.DataStoreFactory } and the {@link sonia.scm.store.DataStore} classes to store data
 * Please use the {@link sonia.scm.store.BlobStoreFactory } and the {@link sonia.scm.store.BlobStore} classes to store binary files
 * Please use the {@link sonia.scm.store.ConfigurationStoreFactory} and the {@link sonia.scm.store.ConfigurationStore} classes  to store configurations
 *
 * @author Mohamed Karray
 * @since 2.0.0
 */
public final class InitialRepositoryLocationResolver {

  private static final String DEFAULT_REPOSITORY_PATH = "repositories";
  public static final String REPOSITORIES_NATIVE_DIRECTORY = "data";
  private SCMContextProvider context;
  private FileSystem fileSystem;


  @Inject
  public InitialRepositoryLocationResolver(SCMContextProvider context, FileSystem fileSystem) {
    this.context = context;
    this.fileSystem = fileSystem;
  }

  public File getBaseDirectory() {
    return new File(context.getBaseDirectory(), DEFAULT_REPOSITORY_PATH);
  }

  public File createDirectory(Repository repository) {
    File initialRepoFolder = getDirectory(getDefaultRepositoryPath(), repository);
    try {
      fileSystem.create(initialRepoFolder);
    } catch (IOException e) {
      throw new InternalRepositoryException(repository, "Cannot create repository directory for "+repository.getNamespaceAndName(),  e);
    }
    return initialRepoFolder;
  }

  public File getDirectory(String defaultRepositoryRelativePath, Repository repository) {
    return new File(context.getBaseDirectory(), defaultRepositoryRelativePath + File.separator + repository.getId());
  }

  public String getDefaultRepositoryPath() {
    return DEFAULT_REPOSITORY_PATH ;
  }

  public String getRelativePath(String absolutePath) {
    return absolutePath.replaceFirst(context.getBaseDirectory().getAbsolutePath()+"/", "");
  }
}
