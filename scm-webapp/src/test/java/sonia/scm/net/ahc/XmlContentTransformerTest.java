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

package sonia.scm.net.ahc;

import com.google.common.io.ByteSource;
import java.io.IOException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Sebastian Sdorra
 */
public class XmlContentTransformerTest {

  private final XmlContentTransformer transformer = new XmlContentTransformer();

  @Test
  public void testIsResponsible()
  {
    assertTrue(transformer.isResponsible(String.class, "application/xml"));
    assertTrue(transformer.isResponsible(String.class, "application/xml;charset=UTF-8"));
    assertFalse(transformer.isResponsible(String.class, "text/plain"));
  }
  
  @Test
  public void testMarshallAndUnmarshall() throws IOException{
    ByteSource bs = transformer.marshall(new TestObject("test"));
    TestObject to = transformer.unmarshall(TestObject.class, bs);
    assertEquals("test", to.value);
  }
  
  @SuppressWarnings("unchecked")
  @Test(expected = ContentTransformerException.class)
  public void testUnmarshallIOException() throws IOException{
    ByteSource bs = mock(ByteSource.class);
    when(bs.openBufferedStream()).thenThrow(IOException.class);
    transformer.unmarshall(String.class, bs);
  }
  
  private static class TestObject2 {}
  
  @XmlRootElement(name = "test")
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class TestObject {
  
    private String value;

    public TestObject()
    {
    }
    
    public TestObject(String value)
    {
      this.value = value;
    }
    
  }

}