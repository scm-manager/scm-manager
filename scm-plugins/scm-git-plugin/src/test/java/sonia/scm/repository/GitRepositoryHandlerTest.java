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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.schedule.Scheduler;
import sonia.scm.store.ConfigurationStoreFactory;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class GitRepositoryHandlerTest extends SimpleRepositoryHandlerTestBase {

  @Mock
  private Scheduler scheduler;

  @Mock
  private ConfigurationStoreFactory factory;

  @Mock
  private GitWorkingCopyFactory gitWorkingCopyFactory;


  @Override
  protected void checkDirectory(File directory) {
    File head = new File(directory, "HEAD");

    assertTrue(head.exists());
    assertTrue(head.isFile());

    File config = new File(directory, "config");

    assertTrue(config.exists());
    assertTrue(config.isFile());

    File refs = new File(directory, "refs");

    assertTrue(refs.exists());
    assertTrue(refs.isDirectory());
  }

  @Before
  public void initFactory() {
    when(factory.withType(any())).thenCallRealMethod();
  }

  @Override
  protected RepositoryHandler createRepositoryHandler(ConfigurationStoreFactory factory,
                                                      RepositoryLocationResolver locationResolver,
                                                      File directory) {
    GitRepositoryHandler repositoryHandler = new GitRepositoryHandler(factory,
      scheduler, locationResolver, gitWorkingCopyFactory, null);
    repositoryHandler.init(contextProvider);

    GitConfig config = new GitConfig();

    // TODO fix event bus exception
    repositoryHandler.setConfig(config);

    return repositoryHandler;
  }

  @Test
  public void getDirectory() {
    GitRepositoryHandler repositoryHandler = new GitRepositoryHandler(factory,
      scheduler, locationResolver, gitWorkingCopyFactory, null);
    GitConfig config = new GitConfig();
    config.setDisabled(false);
    config.setGcExpression("gc exp");

    repositoryHandler.setConfig(config);

    initRepository();
    File path = repositoryHandler.getDirectory(repository.getId());
    assertEquals(repoPath.toString() + File.separator + RepositoryDirectoryHandler.REPOSITORIES_NATIVE_DIRECTORY, path.getAbsolutePath());
  }

  @Test
  public void shouldSetHeadToDefaultRepository() {
    GitRepositoryHandler repositoryHandler = new GitRepositoryHandler(factory,
      scheduler, locationResolver, gitWorkingCopyFactory, null);

    GitConfig config = new GitConfig();
    config.setDefaultBranch("other");

    repositoryHandler.setConfig(config);

    File nativeRepoDirectory = initRepository();

    repositoryHandler.create(repository);

    assertThat(new File(nativeRepoDirectory, "HEAD")).hasContent("ref: refs/heads/other");
  }
}
