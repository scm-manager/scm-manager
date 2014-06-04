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

//~--- JDK imports ------------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * This class is a pretty print wrapper for XMLStreamWriter.
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
public final class IndentXMLStreamWriter implements XMLStreamWriter
{

  /** line separator */
  @VisibleForTesting
  static final String LINE_SEPARATOR =
    System.getProperty("line.separator");

  //~--- constructors ---------------------------------------------------------

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

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() throws XMLStreamException
  {
    writer.close();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void flush() throws XMLStreamException
  {
    writer.flush();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeAttribute(String localName, String value)
    throws XMLStreamException
  {
    writer.writeAttribute(localName, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeAttribute(String prefix, String namespaceURI,
    String localName, String value)
    throws XMLStreamException
  {
    writer.writeAttribute(prefix, namespaceURI, localName, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeAttribute(String namespaceURI, String localName,
    String value)
    throws XMLStreamException
  {
    writer.writeAttribute(namespaceURI, localName, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeCData(String data) throws XMLStreamException
  {
    writer.writeCData(data);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeCharacters(String text) throws XMLStreamException
  {
    writer.writeCharacters(text);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeCharacters(char[] text, int start, int len)
    throws XMLStreamException
  {
    writer.writeCharacters(text, start, len);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeComment(String data) throws XMLStreamException
  {
    indent();
    writer.writeComment(data);
    unindent();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeDTD(String dtd) throws XMLStreamException
  {
    writer.writeDTD(dtd);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeDefaultNamespace(String namespaceURI)
    throws XMLStreamException
  {
    writer.writeDefaultNamespace(namespaceURI);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeEmptyElement(String namespaceURI, String localName)
    throws XMLStreamException
  {
    indent();
    writer.writeEmptyElement(namespaceURI, localName);
    unindent();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeEmptyElement(String prefix, String localName,
    String namespaceURI)
    throws XMLStreamException
  {
    indent();
    writer.writeEmptyElement(prefix, localName, namespaceURI);
    unindent();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeEmptyElement(String localName) throws XMLStreamException
  {
    indent();
    writer.writeEmptyElement(localName);
    unindent();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeEndDocument() throws XMLStreamException
  {
    writer.writeEndDocument();
    writeLineSeparator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeEndElement() throws XMLStreamException
  {
    unindent();
    writer.writeEndElement();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeEntityRef(String name) throws XMLStreamException
  {
    writer.writeEntityRef(name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeNamespace(String prefix, String namespaceURI)
    throws XMLStreamException
  {
    writer.writeNamespace(prefix, namespaceURI);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeProcessingInstruction(String target)
    throws XMLStreamException
  {
    writer.writeProcessingInstruction(target);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeProcessingInstruction(String target, String data)
    throws XMLStreamException
  {
    writer.writeProcessingInstruction(target, data);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeStartDocument() throws XMLStreamException
  {
    writer.writeStartDocument();
    writeLineSeparator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeStartDocument(String version) throws XMLStreamException
  {
    writer.writeStartDocument(version);
    writeLineSeparator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeStartDocument(String encoding, String version)
    throws XMLStreamException
  {
    writer.writeStartDocument(encoding, version);
    writeLineSeparator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeStartElement(String localName) throws XMLStreamException
  {
    indent();
    writer.writeStartElement(localName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeStartElement(String namespaceURI, String localName)
    throws XMLStreamException
  {
    indent();
    writer.writeStartElement(namespaceURI, localName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeStartElement(String prefix, String localName,
    String namespaceURI)
    throws XMLStreamException
  {
    indent();
    writer.writeStartElement(prefix, localName, namespaceURI);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override
  public NamespaceContext getNamespaceContext()
  {
    return writer.getNamespaceContext();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getPrefix(String uri) throws XMLStreamException
  {
    return writer.getPrefix(uri);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getProperty(String name) throws IllegalArgumentException
  {
    return writer.getProperty(name);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDefaultNamespace(String uri) throws XMLStreamException
  {
    writer.setDefaultNamespace(uri);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setNamespaceContext(NamespaceContext context)
    throws XMLStreamException
  {
    writer.setNamespaceContext(context);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setPrefix(String prefix, String uri) throws XMLStreamException
  {
    writer.setPrefix(prefix, uri);
  }
  
  public void writeLineSeparator() throws XMLStreamException{
    writer.writeCharacters(LINE_SEPARATOR);
  }

  //~--- methods --------------------------------------------------------------

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
  private String indent = "    ";

  /** current level */
  private int level = 0;

  /** last element was a start */
  private boolean lastWasStart = false;

  /** wrapped xml writer */
  private XMLStreamWriter writer;
}
