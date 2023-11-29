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


import jakarta.annotation.Nonnull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.autoconfig.AutoConfiguratorProvider;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.spi.HgVersionCommand;
import sonia.scm.store.ConfigurationStoreFactory;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//~--- JDK imports ------------------------------------------------------------

/**
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class HgRepositoryHandlerTest extends SimpleRepositoryHandlerTestBase {

  @Mock
  private ConfigurationStoreFactory factory;

  @Override
  protected void checkDirectory(File directory) {
    File hgDirectory = new File(directory, ".hg");

    assertTrue(hgDirectory.exists());
    assertTrue(hgDirectory.isDirectory());
  }

  @Before
  public void initFactory() {
    when(factory.withType(any())).thenCallRealMethod();
  }

  @Override
  protected RepositoryHandler createRepositoryHandler(ConfigurationStoreFactory factory, RepositoryLocationResolver locationResolver, File directory) {
    HgRepositoryHandler handler = createHandler(factory, locationResolver);

    handler.init(contextProvider);
    HgTestUtil.checkForSkip(handler);

    return handler;
  }

  @Test
  public void getDirectory() {
    HgRepositoryHandler repositoryHandler = createHandler(factory, locationResolver);

    HgGlobalConfig hgGlobalConfig = new HgGlobalConfig();
    hgGlobalConfig.setHgBinary("hg");
    repositoryHandler.setConfig(hgGlobalConfig);

    initRepository();
    File path = repositoryHandler.getDirectory(repository.getId());
    assertEquals(repoPath.toString() + File.separator + RepositoryDirectoryHandler.REPOSITORIES_NATIVE_DIRECTORY, path.getAbsolutePath());
  }

  @Nonnull
  private HgRepositoryHandler createHandler(ConfigurationStoreFactory factory, RepositoryLocationResolver locationResolver) {
    AutoConfiguratorProvider provider = new AutoConfiguratorProvider(new HgVerifier());
    return new HgRepositoryHandler(factory, locationResolver, null, null, provider.get());
  }

  @Test
  public void shouldReturnVersionInformation() {
    PluginLoader pluginLoader = mock(PluginLoader.class);
    when(pluginLoader.getUberClassLoader()).thenReturn(HgRepositoryHandler.class.getClassLoader());

    HgVersionCommand versionCommand = mock(HgVersionCommand.class);
    when(versionCommand.get()).thenReturn("python/3.7.2 mercurial/5.2.0");

    HgRepositoryHandler handler = new HgRepositoryHandler(
      factory, locationResolver, pluginLoader, null, new AutoConfiguratorProvider(new HgVerifier()).get()
    );

    String versionInformation = handler.getVersionInformation(versionCommand);
    assertThat(versionInformation).startsWith("scm-hg-version/").endsWith("python/3.7.2 mercurial/5.2.0");
  }
}
