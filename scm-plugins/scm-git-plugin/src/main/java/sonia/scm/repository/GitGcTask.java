/***
 * Copyright (c) 2015, Sebastian Sdorra
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * https://bitbucket.org/sdorra/scm-manager
 * 
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
 * @author Sebastian Sdorra
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
        logger.info("start git gc for repository {}", repository.getName());
        Stopwatch sw = Stopwatch.createStarted();
        gc(repository);
        logger.debug("gc of repository {} has finished after {}", repository.getName(), sw.stop());
      } 
      else 
      {
        logger.debug("skip non valid/healthy repository {}", repository.getName());
      }
    } 
    else 
    {
      logger.trace("skip non git repository {}", repository.getName());
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
    File file = repositoryHandler.getDirectory(repository);
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
