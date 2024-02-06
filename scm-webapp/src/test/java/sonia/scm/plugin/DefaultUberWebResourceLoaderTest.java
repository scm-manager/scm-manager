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
    
package sonia.scm.plugin;


import com.google.common.collect.Lists;
import jakarta.servlet.ServletContext;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.Stage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DefaultUberWebResourceLoaderTest extends WebResourceLoaderTestBase
{

  private static URL BITBUCKET;

  private static URL GITHUB;

  private static URL SCM_MANAGER;



  @BeforeClass
  public static void prepare() throws MalformedURLException
  {
    SCM_MANAGER = new URL("https://www.scm-manager.org");
    BITBUCKET = new URL("https://bitbucket.org");
    GITHUB = new URL("https://github.com");
  }

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


  private InstalledPlugin createPluginWrapper(File directory)
  {
    return createPluginWrapper(directory.toPath());
  }


  private InstalledPlugin createPluginWrapper(Path directory)
  {
    return new InstalledPlugin(null, null, new PathWebResourceLoader(directory),
      directory, false);
  }

  //~--- fields ---------------------------------------------------------------

  @Mock
  private ServletContext servletContext;
}
