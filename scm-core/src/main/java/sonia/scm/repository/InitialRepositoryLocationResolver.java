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
public class InitialRepositoryLocationResolver {

  public static final String DEFAULT_REPOSITORY_PATH = "repositories";

  private final SCMContextProvider context;

  @Inject
  public InitialRepositoryLocationResolver(SCMContextProvider context) {
    this.context = context;
  }

  File getDefaultDirectory(Repository repository) {
    String initialRepoFolder = getRelativeRepositoryPath(repository);
    return new File(context.getBaseDirectory(), initialRepoFolder);
  }

  File getDefaultNativeDirectory(Repository repository) {
    String initialRepoFolder = getRelativeRepositoryPath(repository);
    return new File(context.getBaseDirectory(), initialRepoFolder + "/data");
  }

  public String getRelativeRepositoryPath(Repository repository) {
    return DEFAULT_REPOSITORY_PATH + File.separator + repository.getId();
  }
}
