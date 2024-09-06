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

package sonia.scm.xml;


import org.junit.Assert;
import org.junit.Test;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;


public class IndentXMLStreamWriterTest
{


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
