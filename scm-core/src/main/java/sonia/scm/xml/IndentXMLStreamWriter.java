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

import com.google.common.annotations.VisibleForTesting;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * This class is a pretty print wrapper for XMLStreamWriter.
 *
 * @since 1.31
 */
public final class IndentXMLStreamWriter implements XMLStreamWriter, AutoCloseable
{

  @VisibleForTesting
  static final String LINE_SEPARATOR =
    System.getProperty("line.separator");


  /**
   * Constructs a new IndentXMLStreamWriter.
   *
   * @param writer writer to wrap
   */
  public IndentXMLStreamWriter(XMLStreamWriter writer)
  {
    this.writer = writer;
  }

  /**
   * Constructs a new IndentXMLStreamWriter.
   *
   * @param writer writer to wrap
   * @param indent indent string
   */
  public IndentXMLStreamWriter(XMLStreamWriter writer, String indent)
  {
    this.writer = writer;
    this.indent = indent;
  }


 
  @Override
  public void close() throws XMLStreamException
  {
    writer.close();
  }

 
  @Override
  public void flush() throws XMLStreamException
  {
    writer.flush();
  }

 
  @Override
  public void writeAttribute(String localName, String value)
    throws XMLStreamException
  {
    writer.writeAttribute(localName, value);
  }

 
  @Override
  public void writeAttribute(String prefix, String namespaceURI,
    String localName, String value)
    throws XMLStreamException
  {
    writer.writeAttribute(prefix, namespaceURI, localName, value);
  }

 
  @Override
  public void writeAttribute(String namespaceURI, String localName,
    String value)
    throws XMLStreamException
  {
    writer.writeAttribute(namespaceURI, localName, value);
  }

 
  @Override
  public void writeCData(String data) throws XMLStreamException
  {
    writer.writeCData(data);
  }

 
  @Override
  public void writeCharacters(String text) throws XMLStreamException
  {
    writer.writeCharacters(text);
  }

 
  @Override
  public void writeCharacters(char[] text, int start, int len)
    throws XMLStreamException
  {
    writer.writeCharacters(text, start, len);
  }

 
  @Override
  public void writeComment(String data) throws XMLStreamException
  {
    indent();
    writer.writeComment(data);
    unindent();
  }

 
  @Override
  public void writeDTD(String dtd) throws XMLStreamException
  {
    writer.writeDTD(dtd);
  }

 
  @Override
  public void writeDefaultNamespace(String namespaceURI)
    throws XMLStreamException
  {
    writer.writeDefaultNamespace(namespaceURI);
  }

 
  @Override
  public void writeEmptyElement(String namespaceURI, String localName)
    throws XMLStreamException
  {
    indent();
    writer.writeEmptyElement(namespaceURI, localName);
    unindent();
  }

 
  @Override
  public void writeEmptyElement(String prefix, String localName,
    String namespaceURI)
    throws XMLStreamException
  {
    indent();
    writer.writeEmptyElement(prefix, localName, namespaceURI);
    unindent();
  }

 
  @Override
  public void writeEmptyElement(String localName) throws XMLStreamException
  {
    indent();
    writer.writeEmptyElement(localName);
    unindent();
  }

 
  @Override
  public void writeEndDocument() throws XMLStreamException
  {
    writer.writeEndDocument();
    writeLineSeparator();
  }

 
  @Override
  public void writeEndElement() throws XMLStreamException
  {
    unindent();
    writer.writeEndElement();
  }

 
  @Override
  public void writeEntityRef(String name) throws XMLStreamException
  {
    writer.writeEntityRef(name);
  }

 
  @Override
  public void writeNamespace(String prefix, String namespaceURI)
    throws XMLStreamException
  {
    writer.writeNamespace(prefix, namespaceURI);
  }

 
  @Override
  public void writeProcessingInstruction(String target)
    throws XMLStreamException
  {
    writer.writeProcessingInstruction(target);
  }

 
  @Override
  public void writeProcessingInstruction(String target, String data)
    throws XMLStreamException
  {
    writer.writeProcessingInstruction(target, data);
  }

 
  @Override
  public void writeStartDocument() throws XMLStreamException
  {
    writer.writeStartDocument();
    writeLineSeparator();
  }

 
  @Override
  public void writeStartDocument(String version) throws XMLStreamException
  {
    writer.writeStartDocument(version);
    writeLineSeparator();
  }

 
  @Override
  public void writeStartDocument(String encoding, String version)
    throws XMLStreamException
  {
    writer.writeStartDocument(encoding, version);
    writeLineSeparator();
  }

 
  @Override
  public void writeStartElement(String localName) throws XMLStreamException
  {
    indent();
    writer.writeStartElement(localName);
  }

 
  @Override
  public void writeStartElement(String namespaceURI, String localName)
    throws XMLStreamException
  {
    indent();
    writer.writeStartElement(namespaceURI, localName);
  }

 
  @Override
  public void writeStartElement(String prefix, String localName,
    String namespaceURI)
    throws XMLStreamException
  {
    indent();
    writer.writeStartElement(prefix, localName, namespaceURI);
  }


 
  @Override
  public NamespaceContext getNamespaceContext()
  {
    return writer.getNamespaceContext();
  }

 
  @Override
  public String getPrefix(String uri) throws XMLStreamException
  {
    return writer.getPrefix(uri);
  }

 
  @Override
  public Object getProperty(String name) throws IllegalArgumentException
  {
    return writer.getProperty(name);
  }


 
  @Override
  public void setDefaultNamespace(String uri) throws XMLStreamException
  {
    writer.setDefaultNamespace(uri);
  }

 
  @Override
  public void setNamespaceContext(NamespaceContext context)
    throws XMLStreamException
  {
    writer.setNamespaceContext(context);
  }

 
  @Override
  public void setPrefix(String prefix, String uri) throws XMLStreamException
  {
    writer.setPrefix(prefix, uri);
  }
  
  public void writeLineSeparator() throws XMLStreamException{
    writer.writeCharacters(LINE_SEPARATOR);
  }


  /**
   * Write indent spaces for start elements.
   *
   *
   * @throws XMLStreamException
   */
  private void indent() throws XMLStreamException
  {
    lastWasStart = true;
    writeIndent(level);
    level += 1;
  }

  /**
   * Write indent spaces for end elements.
   *
   *
   * @throws XMLStreamException
   */
  private void unindent() throws XMLStreamException
  {
    level -= 1;

    if (!lastWasStart)
    {
      writeIndent(level);
    }

    if (level == 0)
    {
      writeLineSeparator();
    }

    lastWasStart = false;
  }

  /**
   * Write indent spaces.
   *
   *
   * @param level current level
   *
   * @throws XMLStreamException
   */
  private void writeIndent(int level) throws XMLStreamException
  {
    if (level > 0)
    {
      StringBuilder b = new StringBuilder(level + 1);

      b.append(LINE_SEPARATOR);

      for (int i = 0; i < level; i++)
      {
        b.append(indent);
      }

      writer.writeCharacters(b.toString());
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** indent string */
  private String indent = "  ";

  /** current level */
  private int level = 0;

  /** last element was a start */
  private boolean lastWasStart = false;

  /** wrapped xml writer */
  private XMLStreamWriter writer;
}
