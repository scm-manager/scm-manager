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

package sonia.scm.repository;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.eclipse.jgit.api.GarbageCollectCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes git gc on every git repository. Statistics of the gc process are logged to the info level. The task is
 * disabled by default and must be enabled through the global git configuration.
 *
 * @since 1.47
 */
public class GitGcTask implements Runnable {

  private static final String SP = System.getProperty("line.seperator", "\n");

  private static final Logger logger = LoggerFactory.getLogger(GitGcTask.class);

  private final RepositoryManager repositoryManager;
  private final RepositoryDirectoryHandler repositoryHandler;

  @Inject
  public GitGcTask(RepositoryManager repositoryManager)
  {
    this.repositoryManager = repositoryManager;
    this.repositoryHandler = (RepositoryDirectoryHandler) repositoryManager.getHandler(GitRepositoryHandler.TYPE_NAME);
  }

  @Override
  public void run()
  {
    for (Repository repository : repositoryManager.getAll())
    {
      handle(repository);
    }
  }

  private void handle(Repository repository){
    if (GitRepositoryHandler.TYPE_NAME.equals(repository.getType()))
    {
      if (repository.isValid() && repository.isHealthy())
      {
        logger.info("start git gc for repository {}", repository);
        Stopwatch sw = Stopwatch.createStarted();
        gc(repository);
        logger.debug("gc of repository {} has finished after {}", repository, sw.stop());
      }
      else
      {
        logger.debug("skip non valid/healthy repository {}", repository);
      }
    }
    else
    {
      logger.trace("skip non git repository {}", repository);
    }
  }

  private void appendProperties(StringBuilder buffer, Properties properties){
    for (Map.Entry<Object,Object> entry : properties.entrySet()){
        buffer.append(SP).append(" - ").append(entry.getKey()).append(" = ").append(entry.getValue());
    }
  }

  private String message(Repository repository, Properties statistics, String span){
    StringBuilder buffer = new StringBuilder("gc statistics for ");
    buffer.append(repository.getName()).append(" ").append(span).append(" execution:");
    appendProperties(buffer, statistics);
    return buffer.toString();
  }

  private void statistics(Repository repository, GarbageCollectCommand gcc) throws GitAPIException {
    Properties properties = gcc.getStatistics();
    logger.info(message(repository, properties, "before"));
  }

  private void execute(Repository repository, GarbageCollectCommand gcc) throws GitAPIException {
    Properties properties = gcc.call();
    logger.info(message(repository, properties, "after"));
  }

  private void gc(Repository repository){
    File file = repositoryHandler.getDirectory(repository.getId());
    Git git = null;
    try {
      git = open(file);
      GarbageCollectCommand gcc = git.gc();
      // print statistics before execution, because it looks like
      // jgit returns the statistics after gc has finished
      statistics(repository, gcc);
      execute(repository, gcc);
    }
    catch (IOException ex)
    {
      logger.warn("failed to open git repository", ex);
    }
    catch (GitAPIException ex)
    {
      logger.warn("failed running git gc command", ex);
    }
    finally
    {
      if (git != null){
        git.close();
      }
    }
  }

  /**
   * Opens the git repository. This method is only visible for testing purposes.
   *
   * @param file repository directory
   *
   * @return git for repository
   *
   * @throws IOException
   */
  @VisibleForTesting
  protected Git open(File file) throws IOException {
    return Git.open(file);
  }

}
