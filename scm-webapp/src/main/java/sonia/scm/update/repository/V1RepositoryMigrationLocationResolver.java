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
import sonia.scm.SCMContextProvider;
import sonia.scm.migration.UpdateException;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

class V1RepositoryMigrationLocationResolver {

  private final Map<String, Path> typeDependentPaths = new HashMap<>();
  private final SCMContextProvider contextProvider;

  V1RepositoryMigrationLocationResolver(SCMContextProvider contextProvider) {
    this.contextProvider = contextProvider;
    typeDependentPaths.put("git", readConfiguredPath(contextProvider, "git", "git.xml"));
    typeDependentPaths.put("hg", readConfiguredPath(contextProvider, "hg", "hg.xml"));
    typeDependentPaths.put("svn", readConfiguredPath(contextProvider, "svn", "svn.xml"));
  }

  Path getTypeDependentPath(String type) {
    return typeDependentPaths.computeIfAbsent(type, t -> defaultPath(contextProvider, t));
  }

  private static Path readConfiguredPath(SCMContextProvider contextProvider, String type, String filename) {
    return readConfig(contextProvider, filename)
      .map(v1RepositoryTypeConfig -> v1RepositoryTypeConfig.getRepositoryDirectory().toPath())
      .orElseGet(() -> defaultPath(contextProvider, type));
  }

  private static Path defaultPath(SCMContextProvider contextProvider, String type) {
    return contextProvider.getBaseDirectory().toPath().resolve("repositories").resolve(type);
  }

  private static Optional<V1RepositoryTypeConfig> readConfig(SCMContextProvider contextProvider, String filename) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(V1RepositoryTypeConfig.class);
      File file = new File(new File(contextProvider.getBaseDirectory(), "config"), filename);
      if (file.exists()) {
        Object unmarshal = jaxbContext.createUnmarshaller().unmarshal(file);
        if (unmarshal instanceof V1RepositoryTypeConfig) {
          return of((V1RepositoryTypeConfig) unmarshal);
        }
      }
    } catch (JAXBException e) {
      throw new UpdateException(String.format("could not read configuration file %s for repository type configuration", filename), e);
    }
    return empty();
  }
}
