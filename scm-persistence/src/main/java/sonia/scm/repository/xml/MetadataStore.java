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

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.CopyOnWrite;
import sonia.scm.store.file.StoreConstants;
import sonia.scm.update.UpdateStepRepositoryMetadataAccess;

import java.nio.file.Path;

import static sonia.scm.CopyOnWrite.compute;

public class MetadataStore implements UpdateStepRepositoryMetadataAccess<Path> {

  private static final Logger LOG = LoggerFactory.getLogger(MetadataStore.class);

  private final JAXBContext jaxbContext;

  public MetadataStore() {
    try {
       jaxbContext = JAXBContext.newInstance(Repository.class);
    } catch (JAXBException ex) {
      throw new IllegalStateException("failed to create jaxb context for repository", ex);
    }
  }

  @Override
  public Repository read(Path path) {
    LOG.trace("read repository metadata from {}", path);
    return compute(() -> {
      try {
        return (Repository) jaxbContext.createUnmarshaller().unmarshal(resolveDataPath(path).toFile());
      } catch (JAXBException | IllegalArgumentException ex) {
        throw new InternalRepositoryException(
          ContextEntry.ContextBuilder.entity(Path.class, path.toString()).build(), "failed read repository metadata", ex
        );
      }
    }).withLockedFileForRead(path);
  }

  /**
   * Write the repository metadata to the given path.
   */
  void write(Path path, Repository repository) {
    LOG.trace("write repository metadata of {} to {}", repository, path);
    try {
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      CopyOnWrite.withTemporaryFile(
        temp -> marshaller.marshal(repository, temp.toFile()),
        resolveDataPath(path)
      );
    } catch (JAXBException ex) {
      throw new InternalRepositoryException(repository, "failed write repository metadata", ex);
    }
  }

  private Path resolveDataPath(Path repositoryPath) {
    return repositoryPath.resolve(StoreConstants.REPOSITORY_METADATA.concat(StoreConstants.FILE_EXTENSION));
  }
}
