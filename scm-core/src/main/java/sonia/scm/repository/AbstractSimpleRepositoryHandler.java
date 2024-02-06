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
