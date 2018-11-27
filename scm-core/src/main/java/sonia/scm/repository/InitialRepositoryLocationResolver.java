package sonia.scm.repository;

import sonia.scm.SCMContextProvider;

import javax.inject.Inject;
import java.io.File;

/**
 * A Location Resolver for File based Repository Storage.
 * <p>
 * <b>WARNING:</b> The Locations provided with this class may not be used from the plugins to store any plugin specific files.
 * <p>
 * Please use the {@link sonia.scm.store.DataStoreFactory } and the {@link sonia.scm.store.DataStore} classes to store data<br>
 * Please use the {@link sonia.scm.store.BlobStoreFactory } and the {@link sonia.scm.store.BlobStore} classes to store binary files<br>
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

  public InitialRepositoryLocation getRelativeRepositoryPath(String repositoryId) {
    String relativePath = DEFAULT_REPOSITORY_PATH + File.separator + repositoryId;
    return new InitialRepositoryLocation(new File(context.getBaseDirectory(), relativePath), relativePath);
  }

  public static class InitialRepositoryLocation {
    private final File absolutePath;
    private final String relativePath;

    public InitialRepositoryLocation(File absolutePath, String relativePath) {
      this.absolutePath = absolutePath;
      this.relativePath = relativePath;
    }

    public File getAbsolutePath() {
      return absolutePath;
    }

    public String getRelativePath() {
      return relativePath;
    }
  }
}
