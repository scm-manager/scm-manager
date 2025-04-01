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

package sonia.scm.store.file;

import sonia.scm.migration.UpdateException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.update.NamespaceUpdateIterator;

import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

public class FileNamespaceUpdateIterator implements NamespaceUpdateIterator {

  private final RepositoryLocationResolver locationResolver;
  private final JAXBContext jaxbContext;

  @Inject
  public FileNamespaceUpdateIterator(RepositoryLocationResolver locationResolver) {
    this.locationResolver = locationResolver;
    try {
      jaxbContext = JAXBContext.newInstance(Repository.class);
    } catch (JAXBException ex) {
      throw new IllegalStateException("failed to create jaxb context for repository", ex);
    }
  }

  @Override
  public void forEachNamespace(Consumer<String> namespaceConsumer) {
    Collection<String> namespaces = new HashSet<>();
    locationResolver
      .forClass(Path.class)
      .forAllLocations((repositoryId, path) -> {
        try {
          Repository metadata = (Repository) jaxbContext.createUnmarshaller().unmarshal(path.resolve("metadata.xml").toFile());
          namespaces.add(metadata.getNamespace());
        } catch (JAXBException e) {
          throw new UpdateException("Failed to read metadata for repository " + repositoryId, e);
        }
      });
    namespaces.forEach(namespaceConsumer);
  }
}
