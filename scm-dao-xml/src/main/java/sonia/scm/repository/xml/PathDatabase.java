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

package sonia.scm.repository.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.store.CopyOnWrite;
import sonia.scm.xml.XmlStreams;
import sonia.scm.xml.XmlStreams.AutoCloseableXMLReader;
import sonia.scm.xml.XmlStreams.AutoCloseableXMLWriter;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static sonia.scm.store.CopyOnWrite.execute;

class PathDatabase {

  private static final Logger LOG = LoggerFactory.getLogger(PathDatabase.class);

  private static final String ENCODING = "UTF-8";
  private static final String VERSION = "1.0";

  private static final String ELEMENT_REPOSITORIES = "repositories";
  private static final String ATTRIBUTE_CREATION_TIME = "creation-time";
  private static final String ATTRIBUTE_LAST_MODIFIED = "last-modified";

  private static final String ELEMENT_REPOSITORY = "repository";
  private static final String ATTRIBUTE_ID = "id";

  private final Path storePath;

  PathDatabase(Path storePath){
    this.storePath = storePath;
  }

  void write(long creationTime, long lastModified, Map<String, Path> pathDatabase) {
    ensureParentDirectoryExists();
    LOG.trace("write repository path database to {}", storePath);

    CopyOnWrite.withTemporaryFile(
      temp -> {
        try (AutoCloseableXMLWriter writer = XmlStreams.createWriter(temp)) {
          writer.writeStartDocument(ENCODING, VERSION);

          writeRepositoriesStart(writer, creationTime, lastModified);
          for (Map.Entry<String, Path> e : pathDatabase.entrySet()) {
            writeRepository(writer, e.getKey(), e.getValue());
          }
          writer.writeEndElement();

          writer.writeEndDocument();
        } catch (XMLStreamException | IOException ex) {
          throw new InternalRepositoryException(
            ContextEntry.ContextBuilder.entity(Path.class, storePath.toString()).build(),
            "failed to write repository path database",
            ex
          );
        }
      },
      storePath
    );
  }

  private void ensureParentDirectoryExists() {
    Path parent = storePath.getParent();
    // Files.exists is slow on java 8
    if (!parent.toFile().exists()) {
      try {
        Files.createDirectories(parent);
      } catch (IOException ex) {
        throw new InternalRepositoryException(
          ContextEntry.ContextBuilder.entity(Path.class, parent.toString()).build(),
          "failed to create parent directory",
          ex
        );
      }
    }
  }

  private void writeRepositoriesStart(XMLStreamWriter writer, long creationTime, long lastModified) throws XMLStreamException {
    writer.writeStartElement(ELEMENT_REPOSITORIES);
    writer.writeAttribute(ATTRIBUTE_CREATION_TIME, String.valueOf(creationTime));
    writer.writeAttribute(ATTRIBUTE_LAST_MODIFIED, String.valueOf(lastModified));
  }

  private void writeRepository(XMLStreamWriter writer, String id, Path value) throws XMLStreamException {
    writer.writeStartElement(ELEMENT_REPOSITORY);
    writer.writeAttribute(ATTRIBUTE_ID, id);
    writer.writeCharacters(value.toString());
    writer.writeEndElement();
  }

  void read(OnRepositories onRepositories, OnRepository onRepository) {
    LOG.trace("read repository path database from {}", storePath);
    execute(() -> {
      try (AutoCloseableXMLReader reader = XmlStreams.createReader(storePath)) {

        while (reader.hasNext()) {
          int eventType = reader.next();

          if (eventType == XMLStreamConstants.START_ELEMENT) {
            String element = reader.getLocalName();
            if (ELEMENT_REPOSITORIES.equals(element)) {
              readRepositories(reader, onRepositories);
            } else if (ELEMENT_REPOSITORY.equals(element)) {
              readRepository(reader, onRepository);
            }
          }
        }
      } catch (XMLStreamException | IOException ex) {
        throw new InternalRepositoryException(
          ContextEntry.ContextBuilder.entity(Path.class, storePath.toString()).build(),
          "failed to read repository path database",
          ex
        );
      }
    }).withLockedFileForRead(storePath);
  }

  private void readRepository(XMLStreamReader reader, OnRepository onRepository) throws XMLStreamException {
    String id = reader.getAttributeValue(null, ATTRIBUTE_ID);
    Path path = Paths.get(reader.getElementText());
    onRepository.handle(id, path);
  }

  private void readRepositories(XMLStreamReader reader, OnRepositories onRepositories) {
    String creationTime = reader.getAttributeValue(null, ATTRIBUTE_CREATION_TIME);
    String lastModified = reader.getAttributeValue(null, ATTRIBUTE_LAST_MODIFIED);
    onRepositories.handle(Long.parseLong(creationTime), Long.parseLong(lastModified));
  }

  @FunctionalInterface
  interface OnRepositories {

    void handle(Long creationTime, Long lastModified);

  }

  @FunctionalInterface
  interface OnRepository {

    void handle(String id, Path path);

  }

}
