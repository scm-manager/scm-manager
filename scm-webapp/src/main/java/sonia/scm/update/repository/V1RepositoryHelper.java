package sonia.scm.update.repository;

import sonia.scm.SCMContextProvider;
import sonia.scm.store.StoreConstants;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

class V1RepositoryHelper {

  static File resolveV1File(SCMContextProvider contextProvider, String filename) {
    return contextProvider
      .resolve(
        Paths.get(StoreConstants.CONFIG_DIRECTORY_NAME).resolve(filename)
      ).toFile();
  }

  static Optional<V1RepositoryDatabase> readV1Database(SCMContextProvider contextProvider, String filename) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(V1RepositoryDatabase.class);
    Object unmarshal = jaxbContext.createUnmarshaller().unmarshal(resolveV1File(contextProvider, filename));
    if (unmarshal instanceof V1RepositoryHelper.V1RepositoryDatabase) {
      return of((V1RepositoryHelper.V1RepositoryDatabase) unmarshal);
    } else {
      return empty();
    }
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
