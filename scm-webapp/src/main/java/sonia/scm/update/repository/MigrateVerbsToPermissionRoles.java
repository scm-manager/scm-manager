package sonia.scm.update.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.migration.UpdateException;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.HealthCheckFailure;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.xml.SingleRepositoryUpdateProcessor;
import sonia.scm.security.SystemRepositoryPermissionProvider;
import sonia.scm.version.Version;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Extension
public class MigrateVerbsToPermissionRoles implements UpdateStep {

  public static final Logger LOG = LoggerFactory.getLogger(MigrateVerbsToPermissionRoles.class);

  private final SingleRepositoryUpdateProcessor updateProcessor;
  private final SystemRepositoryPermissionProvider systemRepositoryPermissionProvider;
  private final JAXBContext jaxbContextNewRepository;
  private final JAXBContext jaxbContextOldRepository;

  @Inject
  public MigrateVerbsToPermissionRoles(SingleRepositoryUpdateProcessor updateProcessor, SystemRepositoryPermissionProvider systemRepositoryPermissionProvider) {
    this.updateProcessor = updateProcessor;
    this.systemRepositoryPermissionProvider = systemRepositoryPermissionProvider;
    jaxbContextNewRepository = createJAXBContext(Repository.class);
    jaxbContextOldRepository = createJAXBContext(OldRepository.class);
  }

  @Override
  public void doUpdate() {
    updateProcessor.doUpdate(this::update);
  }

  void update(String repositoryId, Path path) {
    LOG.info("updating repository {}", repositoryId);
    OldRepository oldRepository = readOldRepository(path);
    Repository newRepository = createNewRepository(oldRepository);
    writeNewRepository(path, newRepository);
  }

  private void writeNewRepository(Path path, Repository newRepository) {
    try {
      Marshaller marshaller = jaxbContextNewRepository.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(newRepository, path.resolve("metadata.xml").toFile());
    } catch (JAXBException e) {
      throw new UpdateException("could not read old repository structure", e);
    }
  }

  private OldRepository readOldRepository(Path path) {
    try {
      return (OldRepository) jaxbContextOldRepository.createUnmarshaller().unmarshal(path.resolve("metadata.xml").toFile());
    } catch (JAXBException e) {
      throw new UpdateException("could not read old repository structure", e);
    }
  }

  private Repository createNewRepository(OldRepository oldRepository) {
    Repository repository = new Repository(
      oldRepository.id,
      oldRepository.type,
      oldRepository.namespace,
      oldRepository.name,
      oldRepository.contact,
      oldRepository.description,
      oldRepository.permissions.stream().map(this::updatePermission).toArray(RepositoryPermission[]::new)
    );
    repository.setCreationDate(oldRepository.creationDate);
    repository.setHealthCheckFailures(oldRepository.healthCheckFailures);
    repository.setLastModified(oldRepository.lastModified);
    return repository;
  }

  private RepositoryPermission updatePermission(RepositoryPermission repositoryPermission) {
    return findMatchingRole(repositoryPermission.getVerbs())
      .map(roleName -> copyRepositoryPermissionWithRole(repositoryPermission, roleName))
      .orElse(repositoryPermission);
  }

  private RepositoryPermission copyRepositoryPermissionWithRole(RepositoryPermission repositoryPermission, String roleName) {
    return new RepositoryPermission(repositoryPermission.getName(), roleName, repositoryPermission.isGroupPermission());
  }

  private Optional<String> findMatchingRole(Collection<String> verbs) {
    return systemRepositoryPermissionProvider.availableRoles()
      .stream()
      .filter(r -> roleMatchesVerbs(verbs, r))
      .map(RepositoryRole::getName)
      .findFirst();
  }

  private boolean roleMatchesVerbs(Collection<String> verbs, RepositoryRole r) {
    return verbs.size() == r.getVerbs().size() && r.getVerbs().containsAll(verbs);
  }

  private JAXBContext createJAXBContext(Class<?> clazz) {
    try {
      return JAXBContext.newInstance(clazz);
    } catch (JAXBException e) {
      throw new UpdateException("could not create XML marshaller", e);
    }
  }

  @Override
  public Version getTargetVersion() {
    return Version.parse("2.0.2");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.repository.xml";
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "repositories")
  private static class OldRepository {
    private String contact;
    private Long creationDate;
    private String description;
    @XmlElement(name = "healthCheckFailure")
    @XmlElementWrapper(name = "healthCheckFailures")
    private List<HealthCheckFailure> healthCheckFailures;
    private String id;
    private Long lastModified;
    private String namespace;
    private String name;
    @XmlElement(name = "permission")
    private final Set<RepositoryPermission> permissions = new HashSet<>();
    @XmlElement(name = "public")
    private boolean publicReadable = false;
    private boolean archived = false;
    private String type;
  }
}
