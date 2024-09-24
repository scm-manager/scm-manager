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
