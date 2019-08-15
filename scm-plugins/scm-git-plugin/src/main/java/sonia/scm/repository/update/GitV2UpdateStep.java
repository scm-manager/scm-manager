package sonia.scm.repository.update;

import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import sonia.scm.migration.UpdateException;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.GitConfigHelper;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.update.UpdateStepRepositoryMetadataAccess;
import sonia.scm.version.Version;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static sonia.scm.version.Version.parse;

@Extension
public class GitV2UpdateStep implements UpdateStep {

  private final RepositoryLocationResolver locationResolver;
  private final UpdateStepRepositoryMetadataAccess<Path> repositoryMetadataAccess;

  @Inject
  public GitV2UpdateStep(RepositoryLocationResolver locationResolver, UpdateStepRepositoryMetadataAccess<Path> repositoryMetadataAccess) {
    this.locationResolver = locationResolver;
    this.repositoryMetadataAccess = repositoryMetadataAccess;
  }

  @Override
  public void doUpdate() {
    locationResolver.forClass(Path.class).forAllLocations(
      (repositoryId, path) -> {
        Repository repository = repositoryMetadataAccess.read(path);
        if (isGitDirectory(repository)) {
          try (org.eclipse.jgit.lib.Repository gitRepository = build(path.resolve("data").toFile())) {
            new GitConfigHelper().createScmmConfig(repository, gitRepository);
          } catch (IOException e) {
            throw new UpdateException("could not update repository with id " + repositoryId + " in path " + path, e);
          }
        }
      }
    );
  }

  private org.eclipse.jgit.lib.Repository build(File directory) throws IOException {
    return new FileRepositoryBuilder()
      .setGitDir(directory)
      .readEnvironment()
      .findGitDir()
      .build();
  }

  private boolean isGitDirectory(Repository repository) {
    return GitRepositoryHandler.TYPE_NAME.equals(repository.getType());
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.plugin.git";
  }
}
