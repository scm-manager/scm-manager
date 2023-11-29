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
