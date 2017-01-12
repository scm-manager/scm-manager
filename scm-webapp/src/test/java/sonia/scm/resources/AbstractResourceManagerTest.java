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

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.*;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.plugin.PluginLoader;

/**
 * Unit tests for {@link AbstractResourceManager}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractResourceManagerTest extends ResourceManagerTestBase
{

  private DummyResourceManager resourceManager;
  
  @Before
  public void setUp()
  {
    Set<ResourceHandler> resourceHandlers = ImmutableSet.of(resourceHandler);
    resourceManager = new DummyResourceManager(pluginLoader, resourceHandlers);
  }
  
  /**
   * Test {@link AbstractResourceManager#getScriptResources()} in the correct order.
   * 
   * @throws java.io.IOException
   * 
   * @see <a href="https://goo.gl/ok03l4">Issue 809</a>
   */
  @Test
  public void testGetScriptResources() throws IOException
  {
    appendScriptResources("a/b.js", "z/a.js", "a/a.js");
    List<String> scripts = resourceManager.getScriptResources();
    assertThat(scripts, contains("a/a.js", "a/b.js", "z/a.js"));
  }
  
  private static class DummyResourceManager extends AbstractResourceManager 
  {

    public DummyResourceManager(PluginLoader pluginLoader, Set<ResourceHandler> resourceHandlers)
    {
      super(pluginLoader, resourceHandlers);
    }

    @Override
    protected void collectResources(Map<ResourceKey, Resource> resourceMap)
    {
    }
    
  }
  
}
