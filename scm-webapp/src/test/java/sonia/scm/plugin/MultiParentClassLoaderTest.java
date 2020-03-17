/**
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
import org.mockito.junit.MockitoJUnitRunner;
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
  
  @SuppressWarnings("unchecked")
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
