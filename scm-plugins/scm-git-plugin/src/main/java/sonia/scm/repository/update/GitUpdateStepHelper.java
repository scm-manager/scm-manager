/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository.update;

import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class GitUpdateStepHelper {

  private GitUpdateStepHelper() {}

  static Path determineEffectiveGitFolder(Path path) {
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

  static org.eclipse.jgit.lib.Repository build(File directory) throws IOException {
    return new FileRepositoryBuilder().setGitDir(directory).readEnvironment().findGitDir().build();
  }

  static boolean isGitDirectory(Repository repository) {
    return GitRepositoryHandler.TYPE_NAME.equals(repository.getType());
  }

}
