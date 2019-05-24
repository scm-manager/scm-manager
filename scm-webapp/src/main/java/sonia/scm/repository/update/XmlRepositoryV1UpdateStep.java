package sonia.scm.repository.update;

import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.xml.XmlRepositoryDAO;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.StoreConstants;
import sonia.scm.version.Version;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static sonia.scm.version.Version.parse;

/**
 * Migrates SCM-Manager v1 repository data structure to SCM-Manager v2 data structure.
 * That is:
 * <ul>
 * <li>The old <code>repositories.xml</code> file is read</li>
 * <li>For each repository in this database,
 * <ul>
 * <li>a new entry in the new <code>repository-paths.xml</code> database is written,</li>
 * <li>the data directory is moved or copied to a SCM v2 consistent directory. How this is done
 * can be specified by a strategy (@see {@link MigrationStrategy}), that has to be set in
 * a database file named <code>migration-plan.xml</code></li> (to create this file, use {@link MigrationStrategyDao}),
 * and
 * <li>the new <code>metadata.xml</code> file is created.</li>
 * </ul>
 * </li>
 * </ul>
 */
@Extension
public class XmlRepositoryV1UpdateStep implements UpdateStep {

  private static Logger LOG = LoggerFactory.getLogger(XmlRepositoryV1UpdateStep.class);

  private final SCMContextProvider contextProvider;
  private final XmlRepositoryDAO repositoryDao;
  private final MigrationStrategyDao migrationStrategyDao;
  private final Injector injector;
  private final ConfigurationEntryStore<V1Properties> propertyStore;

  @Inject
  public XmlRepositoryV1UpdateStep(
    SCMContextProvider contextProvider,
    XmlRepositoryDAO repositoryDao,
    MigrationStrategyDao migrationStrategyDao,
    Injector injector,
    ConfigurationEntryStoreFactory configurationEntryStoreFactory
  ) {
    this.contextProvider = contextProvider;
    this.repositoryDao = repositoryDao;
    this.migrationStrategyDao = migrationStrategyDao;
    this.injector = injector;
    this.propertyStore = configurationEntryStoreFactory
      .withType(V1Properties.class)
      .withName("repository-properties-v1")
      .build();
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.repository.xml";
  }

  @Override
  public void doUpdate() throws JAXBException {
    if (!determineV1File().exists()) {
      LOG.info("no v1 repositories database file found");
      return;
    }
    JAXBContext jaxbContext = JAXBContext.newInstance(V1RepositoryDatabase.class);
    readV1Database(jaxbContext).ifPresent(
      v1Database -> {
        v1Database.repositoryList.repositories.forEach(this::readMigrationStrategy);
        v1Database.repositoryList.repositories.forEach(this::update);
      }
    );
  }

  private void update(V1Repository v1Repository) {
    Path destination = handleDataDirectory(v1Repository);
    Repository repository = new Repository(
      v1Repository.id,
      v1Repository.type,
      getNamespace(v1Repository),
      getName(v1Repository),
      v1Repository.contact,
      v1Repository.description,
      createPermissions(v1Repository));
    LOG.info("creating new repository {} with id {} from old repository {} in directory {}", repository.getNamespaceAndName(), repository.getId(), v1Repository.name, destination);
    repositoryDao.add(repository, destination);
    propertyStore.put(v1Repository.id, v1Repository.properties);
  }

  private Path handleDataDirectory(V1Repository v1Repository) {
    MigrationStrategy dataMigrationStrategy = readMigrationStrategy(v1Repository);
    return dataMigrationStrategy.from(injector).migrate(v1Repository.id, v1Repository.name, v1Repository.type);
  }

  private MigrationStrategy readMigrationStrategy(V1Repository v1Repository) {
    return migrationStrategyDao.get(v1Repository.id)
      .orElseThrow(() -> new IllegalStateException("no strategy found for repository with id " + v1Repository.id + " and name " + v1Repository.name));
  }

  private RepositoryPermission[] createPermissions(V1Repository v1Repository) {
    if (v1Repository.permissions == null) {
      return new RepositoryPermission[0];
    }
    return v1Repository.permissions
      .stream()
      .map(this::createPermission)
      .toArray(RepositoryPermission[]::new);
  }

  private RepositoryPermission createPermission(V1Permission v1Permission) {
    LOG.info("creating permission {} for {}", v1Permission.type, v1Permission.name);
    return new RepositoryPermission(v1Permission.name, v1Permission.type, v1Permission.groupPermission);
  }

  private String getNamespace(V1Repository v1Repository) {
    String[] nameParts = getNameParts(v1Repository.name);
    return nameParts.length > 1 ? nameParts[0] : v1Repository.type;
  }

  private String getName(V1Repository v1Repository) {
    String[] nameParts = getNameParts(v1Repository.name);
    return nameParts.length == 1 ? nameParts[0] : concatPathElements(nameParts);
  }

  private String concatPathElements(String[] nameParts) {
    return Arrays.stream(nameParts).skip(1).collect(Collectors.joining("_"));
  }

  private String[] getNameParts(String v1Name) {
    return v1Name.split("/");
  }

  private Optional<V1RepositoryDatabase> readV1Database(JAXBContext jaxbContext) throws JAXBException {
    Object unmarshal = jaxbContext.createUnmarshaller().unmarshal(determineV1File());
    if (unmarshal instanceof V1RepositoryDatabase) {
      return of((V1RepositoryDatabase) unmarshal);
    } else {
      return empty();
    }
  }

  private File determineV1File() {
    File configDirectory = new File(contextProvider.getBaseDirectory(), StoreConstants.CONFIG_DIRECTORY_NAME);
    return new File(configDirectory, "repositories" + StoreConstants.FILE_EXTENSION);
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "permissions")
  private static class V1Permission {
    private boolean groupPermission;
    private String name;
    private String type;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  private static class V1Property {
    private String key;
    private String value;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "properties")
  private static class V1Properties {
    @XmlElement(name = "item")
    private List<V1Property> properties;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "repositories")
  private static class V1Repository {
    private String contact;
    private long creationDate;
    private Long lastModified;
    private String description;
    private String id;
    private String name;
    private boolean isPublic;
    private boolean archived;
    private String type;
    private List<V1Permission> permissions;
    private V1Properties properties;

    @Override
    public String toString() {
      return "V1Repository{" +
        ", contact='" + contact + '\'' +
        ", creationDate=" + creationDate +
        ", lastModified=" + lastModified +
        ", description='" + description + '\'' +
        ", id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", isPublic=" + isPublic +
        ", archived=" + archived +
        ", type='" + type + '\'' +
        '}';
    }
  }

  private static class RepositoryList {
    @XmlElement(name = "repository")
    private List<V1Repository> repositories;
  }

  @XmlRootElement(name = "repository-db")
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class V1RepositoryDatabase {
    private long creationTime;
    private Long lastModified;
    @XmlElement(name = "repositories")
    private RepositoryList repositoryList;
  }
}
