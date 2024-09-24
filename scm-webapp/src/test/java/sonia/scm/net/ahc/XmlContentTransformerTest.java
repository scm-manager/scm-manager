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

package sonia.scm.net.ahc;

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
