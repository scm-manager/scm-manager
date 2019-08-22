/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
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
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.Stage;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultUberWebResourceLoaderTest extends WebResourceLoaderTestBase
{

  /** Field description */
  private static URL BITBUCKET;

  /** Field description */
  private static URL GITHUB;

  /** Field description */
  private static URL SCM_MANAGER;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws MalformedURLException
   */
  @BeforeClass
  public static void prepare() throws MalformedURLException
  {
    SCM_MANAGER = new URL("https://www.scm-manager.org");
    BITBUCKET = new URL("https://bitbucket.org");
    GITHUB = new URL("https://github.com");
  }

  /**
   * Method description
   *
   *
   */
  @Test
  public void testGetResourceFromCache() {
    DefaultUberWebResourceLoader resourceLoader =
      new DefaultUberWebResourceLoader(servletContext,
        new ArrayList<InstalledPlugin>(), Stage.PRODUCTION);

    resourceLoader.getCache().put("/myresource", GITHUB);

    URL resource = resourceLoader.getResource("/myresource");

    assertSame(GITHUB, resource);
  }

  @Test
  public void testGetResourceCacheIsDisableInStageDevelopment() throws MalformedURLException {
    DefaultUberWebResourceLoader resourceLoader = new DefaultUberWebResourceLoader(servletContext, new ArrayList<>(), Stage.DEVELOPMENT);
    when(servletContext.getResource("/scm")).thenAnswer(invocation -> new URL("https://scm-manager.org"));
    URL url = resourceLoader.getResource("/scm");
    URL secondUrl = resourceLoader.getResource("/scm");
    assertNotSame(url, secondUrl);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testGetResourceFromDirectory() throws IOException
  {
    File directory = temp.newFolder();
    File file = file(directory, "myresource");
    InstalledPlugin wrapper = createPluginWrapper(directory);
    List<InstalledPlugin> plugins = Lists.newArrayList(wrapper);
    WebResourceLoader resourceLoader =
      new DefaultUberWebResourceLoader(servletContext, plugins);

    assertEquals(file.toURI().toURL(),
      resourceLoader.getResource("/myresource"));
  }

  /**
   * Method description
   *
   * @throws MalformedURLException
   */
  @Test
  public void testGetResourceFromServletContext() throws MalformedURLException
  {
    when(servletContext.getResource("/myresource")).thenReturn(SCM_MANAGER);

    WebResourceLoader resourceLoader =
      new DefaultUberWebResourceLoader(servletContext, new ArrayList<>());
    URL resource = resourceLoader.getResource("/myresource");

    assertSame(SCM_MANAGER, resource);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testGetResources() throws IOException
  {
    when(servletContext.getResource("/myresource")).thenReturn(BITBUCKET);

    File directory = temp.newFolder();
    File file = file(directory, "myresource");
    InstalledPlugin wrapper = createPluginWrapper(directory);
    List<InstalledPlugin> plugins = Lists.newArrayList(wrapper);

    UberWebResourceLoader resourceLoader =
      new DefaultUberWebResourceLoader(servletContext, plugins);

    assertThat(resourceLoader.getResources("/myresource"),
      containsInAnyOrder(file.toURI().toURL(), BITBUCKET));
  }

  @Test
  public void shouldReturnNullForDirectoryFromServletContext() throws IOException {
    URL url = temp.newFolder().toURI().toURL();
    when(servletContext.getResource("/myresource")).thenReturn(url);

    WebResourceLoader resourceLoader =
      new DefaultUberWebResourceLoader(servletContext, new ArrayList<>());

    assertNull(resourceLoader.getResource("/myresource"));
  }

  @Test
  public void shouldReturnNullForDirectoryFromPlugin() throws IOException {
    URL url = temp.newFolder().toURI().toURL();
    WebResourceLoader loader = mock(WebResourceLoader.class);
    when(loader.getResource("/myresource")).thenReturn(url);

    InstalledPlugin installedPlugin = mock(InstalledPlugin.class);
    when(installedPlugin.getWebResourceLoader()).thenReturn(loader);

    WebResourceLoader resourceLoader =
      new DefaultUberWebResourceLoader(servletContext, Lists.newArrayList(installedPlugin));

    assertNull(resourceLoader.getResource("/myresource"));
  }

  @Test
  public void shouldReturnNullForDirectories() throws IOException {
    URL url = temp.newFolder().toURI().toURL();
    when(servletContext.getResource("/myresource")).thenReturn(url);

    WebResourceLoader loader = mock(WebResourceLoader.class);
    when(loader.getResource("/myresource")).thenReturn(url);

    InstalledPlugin installedPlugin = mock(InstalledPlugin.class);
    when(installedPlugin.getWebResourceLoader()).thenReturn(loader);

    UberWebResourceLoader resourceLoader =
      new DefaultUberWebResourceLoader(servletContext, Lists.newArrayList(installedPlugin));

    List<URL> resources = resourceLoader.getResources("/myresource");
    Assertions.assertThat(resources).isEmpty();
  }

  /**
   * Method description
   *
   *
   * @param directory
   *
   * @return
   */
  private InstalledPlugin createPluginWrapper(File directory)
  {
    return createPluginWrapper(directory.toPath());
  }

  /**
   * Method description
   *
   *
   * @param directory
   *
   * @return
   */
  private InstalledPlugin createPluginWrapper(Path directory)
  {
    return new InstalledPlugin(null, null, new PathWebResourceLoader(directory),
      directory);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Mock
  private ServletContext servletContext;
}
