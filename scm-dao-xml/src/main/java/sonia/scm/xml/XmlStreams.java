package sonia.scm.xml;

import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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

  public static XMLStreamReader createReader(Path path) throws IOException, XMLStreamException {
    return createReader(Files.newBufferedReader(path, Charsets.UTF_8));
  }

  public static XMLStreamReader createReader(File file) throws IOException, XMLStreamException {
    return createReader(file.toPath());
  }

  private static XMLStreamReader createReader(Reader reader) throws XMLStreamException {
    return XMLInputFactory.newInstance().createXMLStreamReader(reader);
  }


  public static IndentXMLStreamWriter createWriter(Path path) throws IOException, XMLStreamException {
    return createWriter(Files.newBufferedWriter(path, Charsets.UTF_8));
  }

  public static IndentXMLStreamWriter createWriter(File file) throws IOException, XMLStreamException {
    return createWriter(file.toPath());
  }

  private static IndentXMLStreamWriter createWriter(Writer writer) throws XMLStreamException {
    return new IndentXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(writer));
  }

}
