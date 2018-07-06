/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
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
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */


package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ConfigurationException;
import sonia.scm.io.CommandResult;
import sonia.scm.io.ExtendedCommand;
import sonia.scm.io.FileSystem;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;

//~--- JDK imports ------------------------------------------------------------

/**
 * @param <C>
 * @author Sebastian Sdorra
 */
public abstract class AbstractSimpleRepositoryHandler<C extends SimpleRepositoryConfig>
  extends AbstractRepositoryHandler<C> implements RepositoryDirectoryHandler {

  public static final String DEFAULT_VERSION_INFORMATION = "unknown";

  public static final String DIRECTORY_REPOSITORY = "repositories";

  public static final String DOT = ".";

  /**
   * the logger for AbstractSimpleRepositoryHandler
   */
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractSimpleRepositoryHandler.class);

  private FileSystem fileSystem;


  public AbstractSimpleRepositoryHandler(ConfigurationStoreFactory storeFactory,
                                         FileSystem fileSystem) {
    super(storeFactory);
    this.fileSystem = fileSystem;
  }

  @Override
  public void create(Repository repository)
    throws RepositoryException, IOException {
    File directory = getDirectory(repository);

    if (directory.exists()) {
      throw RepositoryAlreadyExistsException.create(repository);
    }

    checkPath(directory);

    try {
      fileSystem.create(directory);
      create(repository, directory);
      postCreate(repository, directory);
    } catch (Exception ex) {
      if (directory.exists()) {
        if (logger.isDebugEnabled()) {
          logger.debug(
            "delete repository directory {}, because of failed repository creation",
            directory);
        }

        fileSystem.destroy(directory);
      }

      Throwables.propagateIfPossible(ex, RepositoryException.class,
        IOException.class);
    }
  }

  @Override
  public String createResourcePath(Repository repository) {
    StringBuilder path = new StringBuilder("/");

    path.append(getType().getName()).append("/").append(repository.getId());

    return path.toString();
  }

  @Override
  public void delete(Repository repository)
    throws RepositoryException, IOException {
    File directory = getDirectory(repository);

    if (directory.exists()) {
      fileSystem.destroy(directory);
      cleanupEmptyDirectories(config.getRepositoryDirectory(),
        directory.getParentFile());
    } else if (logger.isWarnEnabled()) {
      logger.warn("repository {} not found", repository);
    }
  }

  @Override
  public void loadConfig() {
    super.loadConfig();

    if (config == null) {
      config = createInitialConfig();

      if (config != null) {
        File repositoryDirectory = config.getRepositoryDirectory();

        if (repositoryDirectory == null) {
          repositoryDirectory = new File(
            baseDirectory,
            DIRECTORY_REPOSITORY.concat(File.separator).concat(
              getType().getName()));
          config.setRepositoryDirectory(repositoryDirectory);
        }

        IOUtil.mkdirs(repositoryDirectory);
        storeConfig();
      }
    }
  }

  @Override
  public void modify(Repository repository)
    throws RepositoryException, IOException {

    // nothing to do
  }

  @Override
  public File getDirectory(Repository repository) {
    File directory = null;

    if (isConfigured()) {
      File repositoryDirectory = config.getRepositoryDirectory();

      directory = new File(repositoryDirectory, repository.getId());

      if (!IOUtil.isChild(repositoryDirectory, directory)) {
        StringBuilder msg = new StringBuilder(directory.getPath());

        msg.append("is not a child of ").append(repositoryDirectory.getPath());

        throw new ConfigurationException(msg.toString());
      }
    } else {
      throw new ConfigurationException("RepositoryHandler is not configured");
    }

    return directory;
  }

  @Override
  public String getVersionInformation() {
    return DEFAULT_VERSION_INFORMATION;
  }

  protected ExtendedCommand buildCreateCommand(Repository repository,
                                               File directory) {
    throw new UnsupportedOperationException("method is not implemented");
  }

  protected void create(Repository repository, File directory)
    throws RepositoryException, IOException {
    ExtendedCommand cmd = buildCreateCommand(repository, directory);
    CommandResult result = cmd.execute();

    if (!result.isSuccessfull()) {
      StringBuilder msg = new StringBuilder("command exit with error ");

      msg.append(result.getReturnCode()).append(" and message: '");
      msg.append(result.getOutput()).append("'");

      throw new RepositoryException(msg.toString());
    }
  }

  protected C createInitialConfig() {
    return null;
  }

  protected void postCreate(Repository repository, File directory)
    throws IOException, RepositoryException {
  }

  /**
   * Returns the content of a classpath resource or the given default content.
   *
   * @param resource       path of a classpath resource
   * @param defaultContent default content to return
   * @return content of a classpath resource or defaultContent
   */
  protected String getStringFromResource(String resource, String defaultContent) {
    String content = defaultContent;

    try {
      URL url = Resources.getResource(resource);

      if (url != null) {
        content = Resources.toString(url, Charsets.UTF_8);
      }
    } catch (IOException ex) {
      logger.error("could not read resource", ex);
    }

    return content;
  }

  /**
   * Returns true if the directory is a repository.
   *
   * @param directory directory to check
   * @return true if the directory is a repository
   * @since 1.9
   */
  protected boolean isRepository(File directory) {
    return new File(directory, DOT.concat(getType().getName())).exists();
  }

  /**
   * Check path for existing repositories
   *
   * @param directory repository target directory
   * @throws RepositoryAlreadyExistsException
   */
  private void checkPath(File directory) throws RepositoryAlreadyExistsException {
    File repositoryDirectory = config.getRepositoryDirectory();
    File parent = directory.getParentFile();

    while ((parent != null) && !repositoryDirectory.equals(parent)) {
      if (logger.isTraceEnabled()) {
        logger.trace("check {} for existing repository", parent);
      }

      if (isRepository(parent)) {
        if (logger.isErrorEnabled()) {
          logger.error("parent path {} is a repository", parent);
        }

        StringBuilder buffer = new StringBuilder("repository with name ");
        buffer.append(directory.getName()).append(" already exists");
        throw new RepositoryAlreadyExistsException(buffer.toString());
      }

      parent = parent.getParentFile();
    }
  }

  private void cleanupEmptyDirectories(File baseDirectory, File directory) {
    if (IOUtil.isChild(baseDirectory, directory)) {
      if (IOUtil.isEmpty(directory)) {

        // TODO use filesystem
        if (directory.delete()) {
          logger.info("successfully deleted directory {}", directory);
          cleanupEmptyDirectories(baseDirectory, directory.getParentFile());
        } else {
          logger.warn("could not delete directory {}", directory);
        }
      } else {
        logger.debug("could not remove non empty directory {}", directory);
      }
    } else {
      logger.warn("directory {} is not a child of {}", directory, baseDirectory);
    }
  }


}
