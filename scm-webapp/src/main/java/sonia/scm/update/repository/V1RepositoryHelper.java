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

package sonia.scm.update.repository;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import sonia.scm.SCMContextProvider;
import sonia.scm.store.file.StoreConstants;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

class V1RepositoryHelper {

  static Optional<File> resolveV1File(SCMContextProvider contextProvider, String filename) {
    File v1XmlFile = contextProvider.resolve(Paths.get(StoreConstants.CONFIG_DIRECTORY_NAME).resolve(filename)).toFile();
    if (v1XmlFile.exists()) {
      return Optional.of(v1XmlFile);
    }
    return Optional.empty();
  }

  static Optional<V1RepositoryDatabase> readV1Database(SCMContextProvider contextProvider, String filename) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(V1RepositoryDatabase.class);
    Optional<File> file = resolveV1File(contextProvider, filename);
    if (file.isPresent()) {
      Object unmarshal = jaxbContext.createUnmarshaller().unmarshal(file.get());
      if (unmarshal instanceof V1RepositoryDatabase) {
        return of((V1RepositoryDatabase) unmarshal);
      } else {
        return empty();
      }
    }
    return empty();
  }

  static class RepositoryList {
    @XmlElement(name = "repository")
    List<V1Repository> repositories;
  }

  @XmlRootElement(name = "repository-db")
  @XmlAccessorType(XmlAccessType.FIELD)
  static class V1RepositoryDatabase {
    long creationTime;
    Long lastModified;
    @XmlElement(name = "repositories")
    RepositoryList repositoryList;
  }
}
