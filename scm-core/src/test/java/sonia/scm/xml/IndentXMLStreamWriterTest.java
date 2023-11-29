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
    
package sonia.scm.xml;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Assert;
import org.junit.Test;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public class IndentXMLStreamWriterTest
{

  /**
   * Method description
   *
   *
   * @throws XMLStreamException
   */
  @Test
  public void testIndent() throws XMLStreamException
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    XMLStreamWriter writer = null;

    try
    {
      writer = new IndentXMLStreamWriter(
        XMLOutputFactory.newFactory().createXMLStreamWriter(baos));
      writer.writeStartDocument();
      writer.writeStartElement("root");
      writer.writeStartElement("message");
      writer.writeCharacters("Hello");
      writer.writeEndElement();
      writer.writeEndElement();
      writer.writeEndDocument();
    }
    finally
    {
      if (writer != null)
      {
        writer.close();
      }
    }

    String content = baos.toString();

    StringBuilder buffer = new StringBuilder("<?xml version=\"1.0\" ?>");
    buffer.append(IndentXMLStreamWriter.LINE_SEPARATOR);
    buffer.append("<root>").append(IndentXMLStreamWriter.LINE_SEPARATOR);
    buffer.append("  <message>Hello</message>");
    buffer.append(IndentXMLStreamWriter.LINE_SEPARATOR);
    buffer.append("</root>").append(IndentXMLStreamWriter.LINE_SEPARATOR);
    
    Assert.assertEquals(buffer.toString(), content);
  }
  
  
}
