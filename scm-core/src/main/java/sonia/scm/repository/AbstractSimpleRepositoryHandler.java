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


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ConfigurationException;
import sonia.scm.io.CommandResult;
import sonia.scm.io.ExtendedCommand;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.store.ConfigurationStoreFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

public abstract class AbstractSimpleRepositoryHandler<C extends RepositoryConfig>
  extends AbstractRepositoryHandler<C> implements RepositoryDirectoryHandler {

  public static final String DEFAULT_VERSION_INFORMATION = "unknown";

 
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractSimpleRepositoryHandler.class);

  private final RepositoryLocationResolver repositoryLocationResolver;
  private final PluginLoader pluginLoader;

  public AbstractSimpleRepositoryHandler(ConfigurationStoreFactory storeFactory,
                                         RepositoryLocationResolver repositoryLocationResolver,
                                         PluginLoader pluginLoader) {
    super(storeFactory);
    this.repositoryLocationResolver = repositoryLocationResolver;
    this.pluginLoader = pluginLoader;
  }

  @Override
  public Repository create(Repository repository) {
    File nativeDirectory = resolveNativeDirectory(repository.getId());
    try {
      create(repository, nativeDirectory);
      postCreate(repository, nativeDirectory);
    } catch (IOException e) {
      throw new InternalRepositoryException(repository, "could not create native repository directory", e);
    }
    return repository;
  }

  @Override
  public void delete(Repository repository) {
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
  public File getDirectory(String repositoryId) {
    File directory;
    if (isConfigured()) {
      directory = resolveNativeDirectory(repositoryId);
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
      URL url = pluginLoader.getUberClassLoader().getResource(resource);

      if (url != null) {
        content = Resources.toString(url, Charsets.UTF_8);
      }
    } catch (IOException ex) {
      logger.error("could not read resource", ex);
    }

    return content;
  }

  private File resolveNativeDirectory(String repositoryId) {
    return repositoryLocationResolver.create(Path.class).getLocation(repositoryId).resolve(REPOSITORIES_NATIVE_DIRECTORY).toFile();
  }
}
