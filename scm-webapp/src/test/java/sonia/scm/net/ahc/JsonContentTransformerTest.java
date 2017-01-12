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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;

import org.junit.Test;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
public class JsonContentTransformerTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testDoNotFailOnUnknownProperties() {
    ByteSource bs = ByteSource.wrap("{\"value\": \"test\", \"other\": \"test2\"}".getBytes(Charsets.UTF_8));
    TestObject obj = transformer.unmarshall(TestObject.class, bs);
    assertEquals("test", obj.value);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testIsResponsible()
  {
    assertTrue(transformer.isResponsible(String.class, "application/json"));
    assertTrue(transformer.isResponsible(String.class, "application/json;charset=UTF-8"));
    assertFalse(transformer.isResponsible(String.class, "text/plain"));
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testMarshallAndUnmarshall() throws IOException
  {
    ByteSource bs = transformer.marshall(new TestObject("test"));
    TestObject to = transformer.unmarshall(TestObject.class, bs);

    assertEquals("test", to.value);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  @Test(expected = ContentTransformerException.class)
  public void testUnmarshallIOException() throws IOException
  {
    ByteSource bs = mock(ByteSource.class);

    when(bs.openBufferedStream()).thenThrow(IOException.class);
    transformer.unmarshall(String.class, bs);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 15/10/21
   * @author         Enter your name here...    
   */
  @XmlRootElement(name = "test")
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class TestObject
  {

    /**
     * Constructs ...
     *
     */
    public TestObject() {}

    /**
     * Constructs ...
     *
     *
     * @param value
     */
    public TestObject(String value)
    {
      this.value = value;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private String value;
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 15/10/21
   * @author         Enter your name here...    
   */
  private static class TestObject2 {}


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final JsonContentTransformer transformer = new JsonContentTransformer();
}
