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

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class FilterInvalidCharXMLStreamWriter implements XMLStreamWriter, AutoCloseable {

  private final XMLStreamWriter writer;

  private final static String invalidXmlCharsPattern = "[" + "\u0000-\u0008" + "\u000B-\u000C" + "\u000E-\u001F" + "\uD800-\uDFFF" + "\uFFFE-\uFFFF" + "]";

  public FilterInvalidCharXMLStreamWriter(XMLStreamWriter writer) {
    this.writer = writer;
  }

  @Override
  public void writeStartElement(String localName) throws XMLStreamException {
    writer.writeStartElement(filterInvalidXmlChars(localName));
  }

  @Override
  public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
    writer.writeStartElement(filterInvalidXmlChars(namespaceURI), filterInvalidXmlChars(localName));
  }

  @Override
  public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
    writer.writeStartElement(
      filterInvalidXmlChars(prefix),
      filterInvalidXmlChars(localName),
      filterInvalidXmlChars(namespaceURI)
    );
  }

  @Override
  public void writeEmptyElement(String localName) throws XMLStreamException {
    writer.writeEmptyElement(filterInvalidXmlChars(localName));
  }

  @Override
  public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
    writer.writeEmptyElement(filterInvalidXmlChars(namespaceURI), filterInvalidXmlChars(localName));
  }

  @Override
  public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
    writer.writeEmptyElement(
      filterInvalidXmlChars(prefix),
      filterInvalidXmlChars(localName),
      filterInvalidXmlChars(namespaceURI)
    );
  }

  @Override
  public void writeEndElement() throws XMLStreamException {
    writer.writeEndElement();
  }

  @Override
  public void writeEndDocument() throws XMLStreamException {
    writer.writeEndDocument();
  }

  @Override
  public void writeAttribute(String localName, String value) throws XMLStreamException {
    writer.writeAttribute(filterInvalidXmlChars(localName), filterInvalidXmlChars(value));
  }

  @Override
  public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
    writer.writeAttribute(
      filterInvalidXmlChars(namespaceURI),
      filterInvalidXmlChars(localName),
      filterInvalidXmlChars(value)
    );
  }

  @Override
  public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
    writer.writeAttribute(
      filterInvalidXmlChars(prefix),
      filterInvalidXmlChars(namespaceURI),
      filterInvalidXmlChars(localName),
      filterInvalidXmlChars(value)
    );
  }

  @Override
  public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
    writer.writeNamespace(filterInvalidXmlChars(prefix), filterInvalidXmlChars(namespaceURI));
  }

  @Override
  public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
    writer.writeDefaultNamespace(filterInvalidXmlChars(namespaceURI));
  }

  @Override
  public void writeComment(String data) throws XMLStreamException {
    writer.writeComment(filterInvalidXmlChars(data));
  }

  @Override
  public void writeProcessingInstruction(String target) throws XMLStreamException {
    writer.writeProcessingInstruction(filterInvalidXmlChars(target));
  }

  @Override
  public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
    writer.writeProcessingInstruction(filterInvalidXmlChars(target), filterInvalidXmlChars(data));
  }

  @Override
  public void writeCData(String data) throws XMLStreamException {
    writer.writeCData(filterInvalidXmlChars(data));
  }

  @Override
  public void writeDTD(String dtd) throws XMLStreamException {
    writer.writeDTD(filterInvalidXmlChars(dtd));
  }

  @Override
  public void writeEntityRef(String name) throws XMLStreamException {
    writer.writeEntityRef(filterInvalidXmlChars(name));
  }

  @Override
  public void writeStartDocument() throws XMLStreamException {
    writer.writeStartDocument();
  }

  @Override
  public void writeStartDocument(String version) throws XMLStreamException {
    writer.writeStartDocument(filterInvalidXmlChars(version));
  }

  @Override
  public void writeStartDocument(String encoding, String version) throws XMLStreamException {
    writer.writeStartDocument(filterInvalidXmlChars(encoding), filterInvalidXmlChars(version));
  }

  @Override
  public void writeCharacters(String text) throws XMLStreamException {
    writer.writeCharacters(filterInvalidXmlChars(text));
  }

  @Override
  public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
    StringBuilder sb = new StringBuilder();
    for(int i = start; i < start + len; ++i) {
      sb.append(text[i]);
    }

    writer.writeCharacters(filterInvalidXmlChars(sb.toString()));
  }

  @Override
  public String getPrefix(String uri) throws XMLStreamException {
    return writer.getPrefix(filterInvalidXmlChars(uri));
  }

  @Override
  public void setPrefix(String prefix, String uri) throws XMLStreamException {
    writer.setPrefix(filterInvalidXmlChars(prefix), filterInvalidXmlChars(uri));
  }

  @Override
  public void setDefaultNamespace(String uri) throws XMLStreamException {
    writer.setDefaultNamespace(filterInvalidXmlChars(uri));
  }

  @Override
  public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
    writer.setNamespaceContext(context);
  }

  @Override
  public NamespaceContext getNamespaceContext() {
    return writer.getNamespaceContext();
  }

  @Override
  public Object getProperty(String name) throws IllegalArgumentException {
    return writer.getProperty(filterInvalidXmlChars(name));
  }

  @Override
  public void close() throws XMLStreamException {
    writer.close();
  }

  @Override
  public void flush() throws XMLStreamException {
    writer.flush();
  }

  private String filterInvalidXmlChars(String input) {
    return input.replaceAll(invalidXmlCharsPattern, "");
  }
}
