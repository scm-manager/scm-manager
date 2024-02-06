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
    
package sonia.scm.net.ahc;


import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class JsonContentTransformerTest
{

   @Test
  public void testDoNotFailOnUnknownProperties() {
    ByteSource bs = ByteSource.wrap("{\"value\": \"test\", \"other\": \"test2\"}".getBytes(Charsets.UTF_8));
    TestObject obj = transformer.unmarshall(TestObject.class, bs);
    assertEquals("test", obj.value);
  }

   @Test
  public void testIsResponsible()
  {
    assertTrue(transformer.isResponsible(String.class, "application/json"));
    assertTrue(transformer.isResponsible(String.class, "application/json;charset=UTF-8"));
    assertFalse(transformer.isResponsible(String.class, "text/plain"));
  }


  @Test
  public void testMarshallAndUnmarshall() throws IOException
  {
    ByteSource bs = transformer.marshall(new TestObject("test"));
    TestObject to = transformer.unmarshall(TestObject.class, bs);

    assertEquals("test", to.value);
  }


  @SuppressWarnings("unchecked")
  @Test(expected = ContentTransformerException.class)
  public void testUnmarshallIOException() throws IOException
  {
    ByteSource bs = mock(ByteSource.class);

    when(bs.openBufferedStream()).thenThrow(IOException.class);
    transformer.unmarshall(String.class, bs);
  }




  @XmlRootElement(name = "test")
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class TestObject
  {

    /**
     * Constructs ...
     *
     */
    public TestObject() {}

  
    public TestObject(String value)
    {
      this.value = value;
    }

    //~--- fields -------------------------------------------------------------

      private String value;
  }



  private static class TestObject2 {}


  //~--- fields ---------------------------------------------------------------

  private final JsonContentTransformer transformer = new JsonContentTransformer();
}
