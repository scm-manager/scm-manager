package sonia.scm.update.repository;

import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.migration.UpdateException;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.xml.XmlRepositoryDAO;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.StoreConstants;
import sonia.scm.update.CoreUpdateStep;
import sonia.scm.update.V1Properties;
import sonia.scm.version.Version;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static sonia.scm.update.V1PropertyReader.REPOSITORY_PROPERTY_READER;
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
 * a database file named <code>migration-plan.xml</code></li> (to create this file, use {@link DefaultMigrationStrategyDAO}),
 * and
 * <li>the new <code>metadata.xml</code> file is created.</li>
 * </ul>
 * </li>
 * </ul>
 */
@Extension
public class XmlRepositoryV1UpdateStep implements CoreUpdateStep {

  private static Logger LOG = LoggerFactory.getLogger(XmlRepositoryV1UpdateStep.class);

  private final SCMContextProvider contextProvider;
  private final XmlRepositoryDAO repositoryDao;
  private final DefaultMigrationStrategyDAO migrationStrategyDao;
  private final Injector injector;
  private final ConfigurationEntryStore<V1Properties> propertyStore;

  @Inject
  public XmlRepositoryV1UpdateStep(
    SCMContextProvider contextProvider,
    XmlRepositoryDAO repositoryDao,
    DefaultMigrationStrategyDAO migrationStrategyDao,
    Injector injector,
    ConfigurationEntryStoreFactory configurationEntryStoreFactory
  ) {
    this.contextProvider = contextProvider;
    this.repositoryDao = repositoryDao;
    this.migrationStrategyDao = migrationStrategyDao;
    this.injector = injector;
    this.propertyStore = configurationEntryStoreFactory
      .withType(V1Properties.class)
      .withName(REPOSITORY_PROPERTY_READER.getStoreName())
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
    if (!resolveV1File().exists()) {
      LOG.info("no v1 repositories database file found");
      return;
    }
    JAXBContext jaxbContext = JAXBContext.newInstance(V1RepositoryDatabase.class);
    readV1Database(jaxbContext).ifPresent(
      v1Database -> {
        v1Database.repositoryList.repositories.forEach(this::readMigrationEntry);
        v1Database.repositoryList.repositories.forEach(this::update);
        backupOldRepositoriesFile();
      }
    );
  }

  public List<V1Repository> getRepositoriesWithoutMigrationStrategies() {
    if (!resolveV1File().exists()) {
      LOG.info("no v1 repositories database file found");
      return emptyList();
    }
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(XmlRepositoryV1UpdateStep.V1RepositoryDatabase.class);
      return readV1Database(jaxbContext)
        .map(v1Database -> v1Database.repositoryList.repositories.stream())
        .orElse(Stream.empty())
        .filter(v1Repository -> !this.findMigrationStrategy(v1Repository).isPresent())
        .collect(Collectors.toList());
    } catch (JAXBException e) {
      throw new UpdateException("could not read v1 repository database", e);
    }
  }

  private void backupOldRepositoriesFile() {
    Path configDir = contextProvider.getBaseDirectory().toPath().resolve(StoreConstants.CONFIG_DIRECTORY_NAME);
    Path oldRepositoriesFile = configDir.resolve("repositories.xml");
    Path backupFile = configDir.resolve("repositories.xml.v1.backup");
    LOG.info("moving old repositories database files to backup file {}", backupFile);
    try {
      Files.move(oldRepositoriesFile, backupFile);
    } catch (IOException e) {
      throw new UpdateException("could not backup old repository database file", e);
    }
  }

  private void update(V1Repository v1Repository) {
    RepositoryMigrationPlan.RepositoryMigrationEntry repositoryMigrationEntry = readMigrationEntry(v1Repository);
    Optional<Path> destination = handleDataDirectory(v1Repository, repositoryMigrationEntry.getDataMigrationStrategy());
    LOG.info("using strategy {} to migrate repository {} with id {} using new namespace {} and name {}",
      repositoryMigrationEntry.getDataMigrationStrategy().getClass(),
      v1Repository.getName(),
      v1Repository.getId(),
      repositoryMigrationEntry.getNewNamespace(),
      repositoryMigrationEntry.getNewName());
    destination.ifPresent(
      newPath -> {
        Repository repository = new Repository(
          v1Repository.getId(),
          v1Repository.getType(),
          repositoryMigrationEntry.getNewNamespace(),
          repositoryMigrationEntry.getNewName(),
          v1Repository.getContact(),
          v1Repository.getDescription(),
          createPermissions(v1Repository));
        LOG.info("creating new repository {} with id {} from old repository {} in directory {}", repository.getNamespaceAndName(), repository.getId(), v1Repository.getName(), newPath);
        repositoryDao.add(repository, newPath);
        propertyStore.put(v1Repository.getId(), v1Repository.getProperties());
      }
    );
  }

  private Optional<Path> handleDataDirectory(V1Repository v1Repository, MigrationStrategy dataMigrationStrategy) {
    return dataMigrationStrategy
      .from(injector)
      .migrate(v1Repository.getId(), v1Repository.getName(), v1Repository.getType());
  }

  private RepositoryMigrationPlan.RepositoryMigrationEntry readMigrationEntry(V1Repository v1Repository) {
    return findMigrationStrategy(v1Repository)
      .orElseThrow(() -> new IllegalStateException("no strategy found for repository with id " + v1Repository.getId() + " and name " + v1Repository.getName()));
  }

  private Optional<RepositoryMigrationPlan.RepositoryMigrationEntry> findMigrationStrategy(V1Repository v1Repository) {
    return migrationStrategyDao.get(v1Repository.getId());
  }

  private RepositoryPermission[] createPermissions(V1Repository v1Repository) {
    if (v1Repository.getPermissions() == null) {
      return new RepositoryPermission[0];
    }
    return v1Repository.getPermissions()
      .stream()
      .map(this::createPermission)
      .toArray(RepositoryPermission[]::new);
  }

  private RepositoryPermission createPermission(V1Permission v1Permission) {
    LOG.info("creating permission {} for {}", v1Permission.getType(), v1Permission.getName());
    return new RepositoryPermission(v1Permission.getName(), v1Permission.getType(), v1Permission.isGroupPermission());
  }

  private Optional<V1RepositoryDatabase> readV1Database(JAXBContext jaxbContext) throws JAXBException {
    Object unmarshal = jaxbContext.createUnmarshaller().unmarshal(resolveV1File());
    if (unmarshal instanceof V1RepositoryDatabase) {
      return of((V1RepositoryDatabase) unmarshal);
    } else {
      return empty();
    }
  }

  private File resolveV1File() {
    return contextProvider
      .resolve(
        Paths.get(StoreConstants.CONFIG_DIRECTORY_NAME).resolve("repositories" + StoreConstants.FILE_EXTENSION)
      ).toFile();
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
