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


@Singleton
@Extension
public class GitRepositoryHandler
  extends AbstractSimpleRepositoryHandler<GitConfig>
{

  public static final String DIRECTORY_REFS = "refs";

  public static final String RESOURCE_VERSION =
    "sonia/scm/version/scm-git-plugin";

  public static final String TYPE_DISPLAYNAME = "Git";

  public static final String TYPE_NAME = "git";


  public static final String DOT_GIT = ".git";

  private static final Logger logger = LoggerFactory.getLogger(GitRepositoryHandler.class);

  public static final RepositoryType TYPE = new RepositoryType(TYPE_NAME,
                                    TYPE_DISPLAYNAME,
                                    GitRepositoryServiceProvider.COMMANDS);

  private static final Object LOCK = new Object();

  private final Scheduler scheduler;

  private final GitWorkingCopyFactory workingCopyFactory;

  private Task task;


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

  
  @Override
  public ImportHandler getImportHandler()
  {
    return new GitImportHandler(this);
  }

  
  @Override
  public RepositoryType getType()
  {
    return TYPE;
  }

  
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

  
  @Override
  protected GitConfig createInitialConfig()
  {
    return new GitConfig();
  }


  
  @Override
  protected Class<GitConfig> getConfigClass()
  {
    return GitConfig.class;
  }
}
