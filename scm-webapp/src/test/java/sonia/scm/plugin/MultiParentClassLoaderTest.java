/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */

package sonia.scm.plugin;

import com.google.common.base.Enums;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiParentClassLoaderTest
{
  
  @Mock
  private ClassLoader parent1;
  
  @Mock
  private ClassLoader parent2;

  private MultiParentClassLoader classLoader;
  
  @Before
  public void setUp(){
     classLoader = new MultiParentClassLoader(parent1, parent2);
  }
  
  @Test(expected = ClassNotFoundException.class)
  public void testClassNotFoundException() throws ClassNotFoundException
  {
    when(parent1.loadClass("string")).thenThrow(ClassNotFoundException.class);
    when(parent2.loadClass("string")).thenThrow(ClassNotFoundException.class);
    classLoader.loadClass("string");
  }
  
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
  
  @Test
  public void testGetResource() throws IOException
  {
    URL url1 = tempFolder.newFile().toURI().toURL();
    URL url2 = tempFolder.newFile().toURI().toURL();
    when(parent1.getResource("resource1")).thenReturn(url1);
    when(parent2.getResource("resource2")).thenReturn(url2);
    
    assertEquals(url1, classLoader.getResource("resource1"));
    assertEquals(url2, classLoader.getResource("resource2"));
    assertNull(classLoader.getResource("resource3"));
  }
  
  @Test
  public void testGetResources() throws IOException{
        URL url1 = tempFolder.newFile().toURI().toURL();
    URL url2 = tempFolder.newFile().toURI().toURL();
    URL url3 = tempFolder.newFile().toURI().toURL();
    when(parent1.getResources("resources")).thenReturn(res(url1, url2));
    when(parent2.getResources("resources")).thenReturn(res(url3));
    
    List<URL> enm = Collections.list(classLoader.getResources("resources"));
    assertThat(enm, containsInAnyOrder(url1, url2, url3));
  }
  
  @Test
  public void testLoadClass() throws ClassNotFoundException{
    when(parent1.loadClass("string")).then(new ClassAnswer(String.class));
    when(parent1.loadClass("int")).then(new ClassAnswer(Integer.class));
    
    assertEquals(String.class, classLoader.loadClass("string"));
    assertEquals(Integer.class, classLoader.loadClass("int"));
  }
  private static class ClassAnswer implements Answer<Object> {
  
    private final Class<?> clazz;

    public ClassAnswer(
      Class<?> clazz)
    {
      this.clazz = clazz;
    }

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable
    {
      return clazz;
    }
  
  }
  
  private Enumeration<URL> res(URL... urls){
    return Collections.enumeration(Arrays.asList(urls));
  }
  
}
