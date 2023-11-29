/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.repository.update;

import jakarta.inject.Inject;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
          final Path effectiveGitPath = determineEffectiveGitFolder(path);
          try (org.eclipse.jgit.lib.Repository gitRepository = build(effectiveGitPath.toFile())) {
            new GitConfigHelper().createScmmConfig(repository, gitRepository);
          } catch (IOException e) {
            throw new UpdateException("could not update repository with id " + repositoryId + " in path " + path, e);
          }
        }
      }
    );
  }

  public Path determineEffectiveGitFolder(Path path) {
    Path bareGitFolder = path.resolve("data");
    Path nonBareGitFolder = bareGitFolder.resolve(".git");
    final Path effectiveGitPath;
    if (Files.exists(nonBareGitFolder)) {
      effectiveGitPath = nonBareGitFolder;
    } else {
      effectiveGitPath = bareGitFolder;
    }
    return effectiveGitPath;
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
