package sonia.scm.repository;

import sonia.scm.migration.UpdateException;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.update.UpdateStepRepositoryMetadataAccess;
import sonia.scm.version.Version;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;

import static sonia.scm.version.Version.parse;

@Extension
public class SvnV1UpdateStep implements UpdateStep {

  private final RepositoryLocationResolver locationResolver;
  private final UpdateStepRepositoryMetadataAccess<Path> repositoryMetadataAccess;

  @Inject
  public SvnV1UpdateStep(RepositoryLocationResolver locationResolver, UpdateStepRepositoryMetadataAccess<Path> repositoryMetadataAccess) {
    this.locationResolver = locationResolver;
    this.repositoryMetadataAccess = repositoryMetadataAccess;
  }

  @Override
  public void doUpdate() {
    locationResolver.forClass(Path.class).forAllLocations(
      (repositoryId, path) -> {
        Repository repository = repositoryMetadataAccess.read(path);
        if (isSvnDirectory(repository)) {
          try {
            new SvnConfigHelper().writeRepositoryId(repository, path.resolve(RepositoryDirectoryHandler.REPOSITORIES_NATIVE_DIRECTORY).toFile());
          } catch (IOException e) {
            throw new UpdateException("could not update repository with id " + repositoryId + " in path " + path, e);
          }
        }
      }
    );
  }

  private boolean isSvnDirectory(Repository repository) {
    return SvnRepositoryHandler.TYPE_NAME.equals(repository.getType());
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.plugin.svn";
  }
}
