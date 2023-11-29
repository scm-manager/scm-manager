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
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import sonia.scm.SCMContextProvider;
import sonia.scm.store.StoreConstants;

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
