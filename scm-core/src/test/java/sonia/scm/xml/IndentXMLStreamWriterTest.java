/**
 * Copyright (c) 2010, Sebastian Sdorra
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


package sonia.scm.xml;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.junit.Assert;

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
    buffer.append("    <message>Hello</message>");
    buffer.append(IndentXMLStreamWriter.LINE_SEPARATOR);
    buffer.append("</root>").append(IndentXMLStreamWriter.LINE_SEPARATOR);
    
    Assert.assertEquals(buffer.toString(), content);
  }
  
  
}
