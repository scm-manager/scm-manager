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

package sonia.scm.repository.work;

import com.google.common.base.Strings;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ConfigValue;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Extension
public class WorkdirProvider implements ServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(WorkdirProvider.class);

  private final File rootDirectory;
  private final RepositoryLocationResolver repositoryLocationResolver;
  private final boolean useRepositorySpecificDir;

  @Inject
  public WorkdirProvider(
    @ConfigValue(key = "workDir", defaultValue = "", description = "Working directory for internal repository operations") String workDir,
    RepositoryLocationResolver repositoryLocationResolver
    ) {
    this(new File(Strings.isNullOrEmpty(workDir) ? System.getProperty("java.io.tmpdir") : workDir, "scm-work"), repositoryLocationResolver, workDir == null);
  }

  public WorkdirProvider(File rootDirectory, RepositoryLocationResolver repositoryLocationResolver, boolean useRepositorySpecificDir) {
    this.rootDirectory = rootDirectory.isAbsolute() ? rootDirectory : new File(System.getProperty("basedir", "."), rootDirectory.getPath());
    LOG.info("using {} as work directory", this.rootDirectory);
    this.repositoryLocationResolver = repositoryLocationResolver;
    this.useRepositorySpecificDir = useRepositorySpecificDir;
    if (!rootDirectory.exists() && !rootDirectory.mkdirs()) {
      throw new IllegalStateException("could not create pool directory " + this.rootDirectory);
    }
  }

  public File createNewWorkdir() {
    return createWorkDir(this.rootDirectory);
  }

  public File createNewWorkdir(String repositoryId) {
    if (useRepositorySpecificDir) {
      Path repositoryLocation = repositoryLocationResolver.forClass(Path.class).getLocation(repositoryId);
      File workDirectoryForRepositoryLocation = getWorkDirectoryForRepositoryLocation(repositoryLocation);
      LOG.debug("creating work dir for repository {} in relative path {}", repositoryId, workDirectoryForRepositoryLocation);
      return createWorkDir(workDirectoryForRepositoryLocation);
    } else {
      LOG.debug("creating work dir for repository {} in global path", repositoryId);
      return createNewWorkdir();
    }
  }

  private File getWorkDirectoryForRepositoryLocation(Path repositoryLocation) {
    return repositoryLocation.resolve("work").toFile();
  }

  private File createWorkDir(File baseDirectory) {
    // recreate base directory when it may be deleted (see https://github.com/scm-manager/scm-manager/issues/1493 for example)
    if (!baseDirectory.exists() && !baseDirectory.mkdirs()) {
      throw new WorkdirCreationException(baseDirectory.toString());
    }
    try {
      File newWorkDir = Files.createTempDirectory(baseDirectory.toPath(), "work-").toFile();
      LOG.debug("created new work dir {}", newWorkDir);
      return newWorkDir;
    } catch (IOException e) {
      throw new WorkdirCreationException(baseDirectory.toString(), e);
    }
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    deleteWorkDirs();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    deleteWorkDirs();
  }

  private void deleteWorkDirs() {
    deleteWorkDirs(rootDirectory);
    repositoryLocationResolver.forClass(Path.class).forAllLocations(
      (repo, repositoryLocation) -> deleteWorkDirs(getWorkDirectoryForRepositoryLocation(repositoryLocation))
    );
  }

  private void deleteWorkDirs(File root) {
    File[] workDirs = root.listFiles();
    if (workDirs != null) {
      LOG.info("deleting {} old work dirs in {}", workDirs.length, root);
      Arrays.stream(workDirs)
        .filter(File::isDirectory)
        .forEach(IOUtil::deleteSilently);
    }
  }
}
