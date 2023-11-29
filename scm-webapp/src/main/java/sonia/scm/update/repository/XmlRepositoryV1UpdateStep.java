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

import com.google.inject.Injector;
import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBException;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static sonia.scm.update.V1PropertyReader.REPOSITORY_PROPERTY_READER;
import static sonia.scm.update.repository.V1RepositoryHelper.resolveV1File;
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

  private final String V1_REPOSITORY_FILENAME = "repositories" + StoreConstants.FILE_EXTENSION;

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
    if (!resolveV1File(contextProvider, V1_REPOSITORY_FILENAME).isPresent()) {
      LOG.info("no v1 repositories database file found");
      return;
    }
    V1RepositoryHelper.readV1Database(contextProvider, V1_REPOSITORY_FILENAME).ifPresent(
      v1Database -> {
        v1Database.repositoryList.repositories.forEach(this::readMigrationEntry);
        v1Database.repositoryList.repositories.forEach(this::update);
        backupOldRepositoriesFile();
      }
    );
  }

  public List<V1Repository> getRepositoriesWithoutMigrationStrategies() {
    if (!resolveV1File(contextProvider, V1_REPOSITORY_FILENAME).isPresent()) {
      LOG.info("no v1 repositories database file found");
      return emptyList();
    }
    try {
      return V1RepositoryHelper.readV1Database(contextProvider, V1_REPOSITORY_FILENAME)
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
        repository.setArchived(v1Repository.isArchived());
        LOG.info("creating new repository {} from old repository {} in directory {}", repository, v1Repository.getName(), newPath);
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

}
