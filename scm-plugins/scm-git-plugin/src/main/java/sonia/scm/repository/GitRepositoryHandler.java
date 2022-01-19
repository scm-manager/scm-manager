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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.spi.GitRepositoryServiceProvider;
import sonia.scm.schedule.Scheduler;
import sonia.scm.schedule.Task;
import sonia.scm.store.ConfigurationStoreFactory;

import java.io.File;
import java.io.IOException;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Extension
public class GitRepositoryHandler
  extends AbstractSimpleRepositoryHandler<GitConfig>
{

  /** Field description */
  public static final String DIRECTORY_REFS = "refs";

  /** Field description */
  public static final String RESOURCE_VERSION =
    "sonia/scm/version/scm-git-plugin";

  /** Field description */
  public static final String TYPE_DISPLAYNAME = "Git";

  /** Field description */
  public static final String TYPE_NAME = "git";


  public static final String DOT_GIT = ".git";

  private static final Logger logger = LoggerFactory.getLogger(GitRepositoryHandler.class);

  /** Field description */
  public static final RepositoryType TYPE = new RepositoryType(TYPE_NAME,
                                    TYPE_DISPLAYNAME,
                                    GitRepositoryServiceProvider.COMMANDS);

  private static final Object LOCK = new Object();

  private final Scheduler scheduler;

  private final GitWorkingCopyFactory workingCopyFactory;

  private Task task;

  //~--- constructors ---------------------------------------------------------

  @Inject
  public GitRepositoryHandler(ConfigurationStoreFactory storeFactory,
                              Scheduler scheduler,
                              RepositoryLocationResolver repositoryLocationResolver,
                              GitWorkingCopyFactory workingCopyFactory,
                              PluginLoader pluginLoader)
  {
    super(storeFactory, repositoryLocationResolver, pluginLoader);
    this.scheduler = scheduler;
    this.workingCopyFactory = workingCopyFactory;
  }

  //~--- get methods ----------------------------------------------------------

  @Override
  public void init(SCMContextProvider context)
  {
    super.init(context);
    scheduleGc(getConfig().getGcExpression());
  }

  @Override
  public void setConfig(GitConfig config)
  {
    scheduleGc(config.getGcExpression());
    super.setConfig(config);
  }

  private void scheduleGc(String expression)
  {
    synchronized (LOCK){
      if ( task != null ){
        logger.debug("cancel existing git gc task");
        task.cancel();
        task = null;
      }
      if (!Strings.isNullOrEmpty(expression))
      {
        logger.info("schedule git gc task with expression {}", expression);
        task = scheduler.schedule(expression, GitGcTask.class);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public ImportHandler getImportHandler()
  {
    return new GitImportHandler(this);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public RepositoryType getType()
  {
    return TYPE;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getVersionInformation()
  {
    return getStringFromResource(RESOURCE_VERSION, DEFAULT_VERSION_INFORMATION);
  }

  public GitWorkingCopyFactory getWorkingCopyFactory() {
    return workingCopyFactory;
  }

  public String getRepositoryId(StoredConfig gitConfig) {
    return new GitConfigHelper().getRepositoryId(gitConfig);
  }

  //~--- methods --------------------------------------------------------------

  @Override
  protected void create(Repository repository, File directory) throws IOException {
    try (org.eclipse.jgit.lib.Repository gitRepository = build(directory)) {
      gitRepository.create(true);
      new GitHeadModifier(this).ensure(repository, config.getDefaultBranch());
      new GitConfigHelper().createScmmConfig(repository, gitRepository);
    }
  }

  private org.eclipse.jgit.lib.Repository build(File directory) throws IOException {
    return new FileRepositoryBuilder()
      .setGitDir(directory)
      .readEnvironment()
      .findGitDir()
      .build();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected GitConfig createInitialConfig()
  {
    return new GitConfig();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Class<GitConfig> getConfigClass()
  {
    return GitConfig.class;
  }
}
