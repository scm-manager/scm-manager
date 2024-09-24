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


import org.junit.Test;
import org.mockito.Mock;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.store.ConfigurationStoreFactory;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


public class SvnRepositoryHandlerTest extends SimpleRepositoryHandlerTestBase {

  @Mock
  private ConfigurationStoreFactory factory;

  @Mock
  private com.google.inject.Provider<RepositoryManager> repositoryManagerProvider;

  private HookContextFactory hookContextFactory = new HookContextFactory(mock(PreProcessorUtil.class));

  private HookEventFacade facade = new HookEventFacade(repositoryManagerProvider, hookContextFactory);

  private SvnRepositoryHandler handler;

  @Override
  protected void postSetUp() throws IOException, RepositoryPathNotFoundException {
    initMocks(this);
    super.postSetUp();
  }

  @Override
  protected void checkDirectory(File directory) {
    File format = new File(directory, "format");

    assertTrue(format.exists());
    assertTrue(format.isFile());

    File db = new File(directory, "db");

    assertTrue(db.exists());
    assertTrue(db.isDirectory());
  }

  @Override
  protected RepositoryHandler createRepositoryHandler(ConfigurationStoreFactory factory,
                                                      RepositoryLocationResolver locationResolver,
                                                      File directory)  {
    SvnRepositoryHandler handler = new SvnRepositoryHandler(factory, facade, locationResolver, null);

    handler.init(contextProvider);

    SvnConfig config = new SvnConfig();

    // TODO fix event bus exception
    handler.setConfig(config);

    return handler;
  }

  @Test
  public void getDirectory() throws IOException {
    when(factory.withType(any())).thenCallRealMethod();
    SvnRepositoryHandler repositoryHandler = new SvnRepositoryHandler(factory,
      facade, locationResolver, null);

    try {
      SvnConfig svnConfig = new SvnConfig();
      repositoryHandler.setConfig(svnConfig);

      initRepository();
      File path = repositoryHandler.getDirectory(repository.getId());
      assertEquals(repoPath.toString() + File.separator + RepositoryDirectoryHandler.REPOSITORIES_NATIVE_DIRECTORY, path.getAbsolutePath());
    } finally {
      repositoryHandler.close();
    }
  }
}
