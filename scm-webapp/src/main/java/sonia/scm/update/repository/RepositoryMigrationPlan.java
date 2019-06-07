package sonia.scm.update.repository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "repository-migration")
class RepositoryMigrationPlan {

  private List<RepositoryEntry> entries;

  RepositoryMigrationPlan() {
    this(new RepositoryEntry[0]);
  }

  RepositoryMigrationPlan(RepositoryEntry... entries) {
    this.entries = new ArrayList<>(asList(entries));
  }

  Optional<MigrationStrategy> get(String repositoryId) {
    return findEntry(repositoryId)
      .map(RepositoryEntry::getDataMigrationStrategy);
  }

  public void set(String repositoryId, MigrationStrategy strategy, String newNamespace, String newName) {
    Optional<RepositoryEntry> entry = findEntry(repositoryId);
    if (entry.isPresent()) {
      entry.get().setStrategy(strategy);
      entry.get().setNewNamespace(newNamespace);
      entry.get().setNewName(newName);
    } else {
      entries.add(new RepositoryEntry(repositoryId, strategy));
    }
  }

  private Optional<RepositoryEntry> findEntry(String repositoryId) {
    return entries.stream()
      .filter(repositoryEntry -> repositoryId.equals(repositoryEntry.repositoryId))
      .findFirst();
  }

  @XmlRootElement(name = "entries")
  @XmlAccessorType(XmlAccessType.FIELD)
  static class RepositoryEntry {

    private String repositoryId;
    private MigrationStrategy dataMigrationStrategy;
    private String newNamespace;
    private String newName;

    RepositoryEntry() {
    }

    RepositoryEntry(String repositoryId, MigrationStrategy dataMigrationStrategy) {
      this.repositoryId = repositoryId;
      this.dataMigrationStrategy = dataMigrationStrategy;
    }

    public MigrationStrategy getDataMigrationStrategy() {
      return dataMigrationStrategy;
    }

    public String getNewNamespace() {
      return newNamespace;
    }

    public String getNewName() {
      return newName;
    }

    private void setStrategy(MigrationStrategy strategy) {
      this.dataMigrationStrategy = strategy;
    }

    private void setNewNamespace(String newNamespace) {
      this.newNamespace = newNamespace;
    }

    private void setNewName(String newName) {
      this.newName = newName;
    }
  }
}
