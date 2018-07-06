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


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.io.DefaultFileSystem;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class SvnRepositoryHandlerTest extends SimpleRepositoryHandlerTestBase {

  @Mock
  private ConfigurationStoreFactory factory;

  @Mock
  private ConfigurationStore store;

  @Mock
  private com.google.inject.Provider<RepositoryManager> repositoryManagerProvider;

  private HookContextFactory hookContextFactory = new HookContextFactory(mock(PreProcessorUtil.class));

  private HookEventFacade facade = new HookEventFacade(repositoryManagerProvider, hookContextFactory);

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
                                                      File directory) {
    SvnRepositoryHandler handler = new SvnRepositoryHandler(factory,
      new DefaultFileSystem(), null);

    handler.init(contextProvider);

    SvnConfig config = new SvnConfig();

    config.setRepositoryDirectory(directory);

    // TODO fix event bus exception
    handler.setConfig(config);

    return handler;
  }

  @Test
  public void getDirectory() {
    when(factory.getStore(any(), any())).thenReturn(store);
    SvnRepositoryHandler repositoryHandler = new SvnRepositoryHandler(factory,
      new DefaultFileSystem(), facade);

    SvnConfig svnConfig = new SvnConfig();
    svnConfig.setRepositoryDirectory(new File("/path"));
    repositoryHandler.setConfig(svnConfig);

    Repository repository = new Repository("id", "svn", "Name");

    File path = repositoryHandler.getDirectory(repository);
    assertEquals("/path/id", path.getAbsolutePath());
    assertTrue(path.getAbsolutePath().endsWith("id"));
  }
}
