package sonia.scm.repository.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.store.CopyOnWrite;
import sonia.scm.store.StoreConstants;
import sonia.scm.update.UpdateStepRepositoryMetadataAccess;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.nio.file.Path;

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

  public Repository read(Path path) {
    LOG.trace("read repository metadata from {}", path);
    try {
      return (Repository) jaxbContext.createUnmarshaller().unmarshal(resolveDataPath(path).toFile());
    } catch (JAXBException ex) {
      throw new InternalRepositoryException(
        ContextEntry.ContextBuilder.entity(Path.class, path.toString()).build(), "failed read repository metadata", ex
      );
    }
  }

  void write(Path path, Repository repository) {
    LOG.trace("write repository metadata of {} to {}", repository.getNamespaceAndName(), path);
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
