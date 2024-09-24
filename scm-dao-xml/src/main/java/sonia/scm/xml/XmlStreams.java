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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.util.StreamReaderDelegate;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class XmlStreams {

  private static final Logger LOG = LoggerFactory.getLogger(XmlStreams.class);

  private XmlStreams() {
  }

  public static void close(XMLStreamWriter writer) {
    if (writer != null) {
      try {
        writer.close();
      } catch (XMLStreamException ex) {
        LOG.error("could not close writer", ex);
      }
    }
  }

  public static void close(XMLStreamReader reader) {
    if (reader != null) {
      try {
        reader.close();
      } catch (XMLStreamException ex) {
        LOG.error("could not close reader", ex);
      }
    }
  }

  public static AutoCloseableXMLReader createReader(Path path) throws IOException, XMLStreamException {
    return createReader(Files.newBufferedReader(path, StandardCharsets.UTF_8));
  }

  public static AutoCloseableXMLReader createReader(File file) throws IOException, XMLStreamException {
    return createReader(file.toPath());
  }

  private static AutoCloseableXMLReader createReader(Reader reader) throws XMLStreamException {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(reader);
    return new AutoCloseableXMLReader(xmlStreamReader, reader);
  }

  public static AutoCloseableXMLWriter createWriter(Path path) throws IOException, XMLStreamException {
    return createWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8));
  }

  public static AutoCloseableXMLWriter createWriter(File file) throws IOException, XMLStreamException {
    return createWriter(file.toPath());
  }

  private static AutoCloseableXMLWriter createWriter(Writer writer) throws XMLStreamException {
    IndentXMLStreamWriter indentXMLStreamWriter = new IndentXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(writer));
    FilterInvalidCharXMLStreamWriter filterInvalidCharXMLStreamWriter = new FilterInvalidCharXMLStreamWriter(indentXMLStreamWriter);
    return new AutoCloseableXMLWriter(filterInvalidCharXMLStreamWriter, writer);
  }

  public static final class AutoCloseableXMLReader extends StreamReaderDelegate implements AutoCloseable {

    private final Closeable closeable;

    public AutoCloseableXMLReader(XMLStreamReader delegate, Closeable closeable) {
      super(delegate);
      this.closeable = closeable;
    }

    @Override
    public void close() throws XMLStreamException {
      super.close();
      try {
        closeable.close();
      } catch (IOException e) {
        throw new XMLStreamException("failed to close nested reader", e);
      }
    }
  }

  public static final class AutoCloseableXMLWriter implements XMLStreamWriter, AutoCloseable {
    private final XMLStreamWriter delegate;
    private final Closeable closeable;

    public AutoCloseableXMLWriter(XMLStreamWriter delegate, Closeable closeable) {
      this.delegate = delegate;
      this.closeable = closeable;
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
      delegate.writeStartElement(localName);
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
      delegate.writeStartElement(namespaceURI, localName);
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
      delegate.writeStartElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
      delegate.writeEmptyElement(namespaceURI, localName);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
      delegate.writeEmptyElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
      delegate.writeEmptyElement(localName);
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
      delegate.writeEndElement();
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
      delegate.writeEndDocument();
    }

    @Override
    public void close() throws XMLStreamException {
      delegate.close();
      try {
        closeable.close();
      } catch (IOException e) {
        throw new XMLStreamException("failed to close nested writer", e);
      }
    }

    @Override
    public void flush() throws XMLStreamException {
      delegate.flush();
    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
      delegate.writeAttribute(localName, value);
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
      delegate.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
      delegate.writeAttribute(namespaceURI, localName, value);
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
      delegate.writeNamespace(prefix, namespaceURI);
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
      delegate.writeDefaultNamespace(namespaceURI);
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
      delegate.writeComment(data);
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
      delegate.writeProcessingInstruction(target);
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
      delegate.writeProcessingInstruction(target, data);
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
      delegate.writeCData(data);
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
      delegate.writeDTD(dtd);
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
      delegate.writeEntityRef(name);
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
      delegate.writeStartDocument();
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
      delegate.writeStartDocument(version);
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
      delegate.writeStartDocument(encoding, version);
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
      delegate.writeCharacters(text);
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
      delegate.writeCharacters(text, start, len);
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
      return delegate.getPrefix(uri);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
      delegate.setPrefix(prefix, uri);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
      delegate.setDefaultNamespace(uri);
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
      delegate.setNamespaceContext(context);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
      return delegate.getNamespaceContext();
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
      return delegate.getProperty(name);
    }
  }
}
