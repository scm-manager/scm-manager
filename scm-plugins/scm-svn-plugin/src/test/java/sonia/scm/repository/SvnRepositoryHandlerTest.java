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
