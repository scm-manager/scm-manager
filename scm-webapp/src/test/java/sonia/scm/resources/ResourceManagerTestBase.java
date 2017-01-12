/***
 * Copyright (c) 2015, Sebastian Sdorra
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
 * https://bitbucket.org/sdorra/scm-manager
 * 
 */

package sonia.scm.resources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import sonia.scm.plugin.Plugin;
import sonia.scm.plugin.PluginCondition;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.PluginResources;
import sonia.scm.plugin.PluginWrapper;
import sonia.scm.plugin.WebResourceLoader;

/**
 * Base class for {@link ResourceManager} tests.
 * 
 * @author Sebastian Sdorra
 */
public abstract class ResourceManagerTestBase 
{
  
  @Mock
  protected ServletContext servletContext;

  @Mock
  protected PluginLoader pluginLoader;

  @Mock
  protected ResourceHandler resourceHandler;
  
  @Mock
  protected WebResourceLoader webResourceLoader;
  
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
  
  /**
   * Append scripts resources to plugin loader.
   * 
   * @param resources resource names
   * 
   * @throws IOException
   */
  protected void appendScriptResources(String... resources) throws IOException
  {
    Set<String> scripts = Sets.newHashSet(resources);
    Set<String> styles = Sets.newHashSet();
    Set<String> dependencies = Sets.newHashSet();
    
    
    Plugin plugin = new Plugin(
      2, 
      new PluginInformation(), 
      new PluginResources(scripts, styles), 
      new PluginCondition(), 
      false, 
      dependencies
    );
    
    Path pluginPath = tempFolder.newFolder().toPath();
    
    PluginWrapper wrapper = new PluginWrapper(
      plugin, 
      Thread.currentThread().getContextClassLoader(), 
      webResourceLoader, 
      pluginPath
    );
    
    List<PluginWrapper> plugins = ImmutableList.of(wrapper);
    
    when(pluginLoader.getInstalledPlugins()).thenReturn(plugins);
  }
}
