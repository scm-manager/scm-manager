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
import sonia.scm.AlreadyExistsException;
import sonia.scm.ConfigurationException;
import sonia.scm.ContextEntry;
import sonia.scm.io.CommandResult;
import sonia.scm.io.ExtendedCommand;
import sonia.scm.io.FileSystem;
import sonia.scm.store.ConfigurationStoreFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

//~--- JDK imports ------------------------------------------------------------

/**
 * @param <C>
 * @author Sebastian Sdorra
 */
public abstract class AbstractSimpleRepositoryHandler<C extends RepositoryConfig>
  extends AbstractRepositoryHandler<C> implements RepositoryDirectoryHandler {

  public static final String DEFAULT_VERSION_INFORMATION = "unknown";

  public static final String DOT = ".";

  /**
   * the logger for AbstractSimpleRepositoryHandler
   */
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractSimpleRepositoryHandler.class);

  private final FileSystem fileSystem;
  private final RepositoryLocationResolver repositoryLocationResolver;
  private final InitialRepositoryLocationResolver initialRepositoryLocationResolver;


  public AbstractSimpleRepositoryHandler(ConfigurationStoreFactory storeFactory,
                                         FileSystem fileSystem,
                                         RepositoryLocationResolver repositoryLocationResolver,
                                         InitialRepositoryLocationResolver initialRepositoryLocationResolver) {
    super(storeFactory);
    this.fileSystem = fileSystem;
    this.repositoryLocationResolver = repositoryLocationResolver;
    this.initialRepositoryLocationResolver = initialRepositoryLocationResolver;
  }

  @Override
  public Repository create(Repository repository) {
    File directory = initialRepositoryLocationResolver.getDefaultNativeDirectory(repository);
    if (directory != null && directory.exists()) {
      throw new AlreadyExistsException(repository);
    }
    try {
      fileSystem.create(directory);
      create(repository, directory);
      postCreate(repository, directory);
      return repository;
    } catch (Exception ex) {
      if (directory != null && directory.exists()) {
        logger.warn("delete repository directory {}, because of failed repository creation", directory);
        try {
          fileSystem.destroy(directory);
        } catch (IOException e) {
          logger.error("Could not destroy directory", e);
        }
      }

      Throwables.propagateIfPossible(ex, AlreadyExistsException.class);
      // This point will never be reached
      return null;
    }
  }

  @Override
  public String createResourcePath(Repository repository) {
    StringBuilder path = new StringBuilder("/");

    path.append(getType().getName()).append("/").append(repository.getId());

    return path.toString();
  }

  @Override
  public void delete(Repository repository) {
    File directory = null;
      directory = repositoryLocationResolver.getRepositoryDirectory(repository);
    try {
      if (directory.exists()) {
        fileSystem.destroy(directory);
      } else {
        logger.warn("repository {} not found", repository.getNamespaceAndName());
      }
    } catch (IOException e) {
      throw new InternalRepositoryException(ContextEntry.ContextBuilder.entity("directory", directory.toString()).in(repository), "could not delete repository directory", e);
    }
  }

  @Override
  public void loadConfig() {
    super.loadConfig();

    if (config == null) {
      config = createInitialConfig();
    }
  }

  @Override
  public void modify(Repository repository) {

    // nothing to do
  }

  @Override
  public File getDirectory(Repository repository) {
    File directory;
    if (isConfigured()) {
        directory = repositoryLocationResolver.getNativeDirectory(repository);
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
    throws IOException {
    ExtendedCommand cmd = buildCreateCommand(repository, directory);
    CommandResult result = cmd.execute();

    if (!result.isSuccessfull()) {
      throw new IOException(("command exit with error " + result.getReturnCode() + " and message: '" + result.getOutput() + "'"));
    }
  }

  protected C createInitialConfig() {
    return null;
  }

  protected void postCreate(Repository repository, File directory)
    throws IOException {
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





}
