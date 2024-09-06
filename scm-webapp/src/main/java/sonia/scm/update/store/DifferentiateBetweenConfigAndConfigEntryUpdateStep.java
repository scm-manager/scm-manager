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

package sonia.scm.update.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import sonia.scm.migration.UpdateException;
import sonia.scm.store.CopyOnWrite;
import sonia.scm.version.Version;
import sonia.scm.xml.XmlStreams;
import sonia.scm.xml.XmlStreams.AutoCloseableXMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static sonia.scm.store.CopyOnWrite.compute;

abstract class DifferentiateBetweenConfigAndConfigEntryUpdateStep {

  private static final Logger LOG = LoggerFactory.getLogger(DifferentiateBetweenConfigAndConfigEntryUpdateStep.class);

  public Version getTargetVersion() {
    return Version.parse("2.0.0");
  }

  public String getAffectedDataType() {
    return "sonia.scm.dao.xml";
  }

  void updateAllInDirectory(Path configDirectory) throws IOException {
    try (Stream<Path> list = Files.list(configDirectory)) {
      list
        .filter(Files::isRegularFile)
        .filter(file -> file.toString().endsWith(".xml"))
        .filter(this::isConfigEntryFile)
        .forEach(this::updateSingleFile);
    }
  }

  private boolean isConfigEntryFile(Path potentialFile) {
    LOG.trace("Testing whether file is config entry file without mark: {}", potentialFile);
    try (AutoCloseableXMLReader reader = XmlStreams.createReader(potentialFile)) {
      reader.nextTag();
      if ("configuration".equals(reader.getLocalName())) {
        reader.nextTag();
        if ("entry".equals(reader.getLocalName())) {
          return true;
        }
      }
    } catch (XMLStreamException | IOException e) {
      throw new UpdateException("Error reading file " + potentialFile, e);
    }
    return false;
  }

  private void updateSingleFile(Path configFile) {
    LOG.info("Updating config entry file: {}", configFile);

    Document configEntryDocument = compute(() -> readAsXmlDocument(configFile)).withLockedFileForRead(configFile);

    configEntryDocument.getDocumentElement().setAttribute("type", "config-entry");

    CopyOnWrite.withTemporaryFile(
      temporaryFile -> writeXmlDocument(configEntryDocument, temporaryFile), configFile
    );
  }

  private void writeXmlDocument(Document configEntryDocument, Path temporaryFile) throws TransformerException {
    TransformerFactory factory = TransformerFactory.newInstance();
//    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    DOMSource domSource = new DOMSource(configEntryDocument);
    try (OutputStream os = Files.newOutputStream(temporaryFile)) {
      factory.newTransformer().transform(domSource, new StreamResult(os));
    } catch (IOException e) {
      throw new UpdateException("Could not write modified config entry file", e);
    }
  }

  private Document readAsXmlDocument(Path configFile) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
//    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

    try (InputStream is = Files.newInputStream(configFile)) {
      return factory.newDocumentBuilder().parse(is);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new UpdateException("Could not read config entry file", e);
    }
  }
}
