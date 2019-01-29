package sonia.scm.repository.xml;


import com.google.common.base.Charsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.SCMContextProvider;
import sonia.scm.io.DefaultFileSystem;
import sonia.scm.io.FileSystem;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryTestData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, TempDirectory.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class XmlRepositoryDAOTest {

  @Mock
  private SCMContextProvider context;

  @Mock
  private InitialRepositoryLocationResolver locationResolver;

  private FileSystem fileSystem = new DefaultFileSystem();

  private XmlRepositoryDAO dao;

  private Path baseDirectory;

  private AtomicLong atomicClock;

  @BeforeEach
  void createDAO(@TempDirectory.TempDir Path baseDirectory) {
    this.baseDirectory = baseDirectory;
    this.atomicClock = new AtomicLong();

    when(locationResolver.getPath("42")).thenReturn(Paths.get("repos", "42"));
    when(locationResolver.getPath("42+1")).thenReturn(Paths.get("repos", "puzzle"));

    when(context.getBaseDirectory()).thenReturn(baseDirectory.toFile());
    when(context.resolve(any(Path.class))).then(ic -> {
      Path path = ic.getArgument(0);
      return baseDirectory.resolve(path);
    });

    dao = createDAO();
  }

  private XmlRepositoryDAO createDAO() {
    Clock clock = mock(Clock.class);
    when(clock.millis()).then(ic -> atomicClock.incrementAndGet());

    return new XmlRepositoryDAO(context, locationResolver, fileSystem, clock);
  }

  @Test
  void shouldReturnXmlType() {
    assertThat(dao.getType()).isEqualTo("xml");
  }

  @Test
  void shouldReturnCreationTimeAfterCreation() {
    long now = atomicClock.get();
    assertThat(dao.getCreationTime()).isEqualTo(now);
  }

  @Test
  void shouldNotReturnLastModifiedAfterCreation() {
    assertThat(dao.getLastModified()).isNull();
  }

  @Test
  void shouldReturnTrueForEachContainsMethod() {
    Repository heartOfGold = createHeartOfGold();
    dao.add(heartOfGold);

    assertThat(dao.contains(heartOfGold)).isTrue();
    assertThat(dao.contains(heartOfGold.getId())).isTrue();
    assertThat(dao.contains(heartOfGold.getNamespaceAndName())).isTrue();
  }

  private Repository createHeartOfGold() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    heartOfGold.setId("42");
    return heartOfGold;
  }

  @Test
  void shouldReturnFalseForEachContainsMethod() {
    Repository heartOfGold = createHeartOfGold();

    assertThat(dao.contains(heartOfGold)).isFalse();
    assertThat(dao.contains(heartOfGold.getId())).isFalse();
    assertThat(dao.contains(heartOfGold.getNamespaceAndName())).isFalse();
  }

  @Test
  void shouldReturnNullForEachGetMethod() {
    assertThat(dao.get("42")).isNull();
    assertThat(dao.get(new NamespaceAndName("hitchhiker","HeartOfGold"))).isNull();
  }

  @Test
  void shouldReturnRepository() {
    Repository heartOfGold = createHeartOfGold();
    dao.add(heartOfGold);

    assertThat(dao.get("42")).isEqualTo(heartOfGold);
    assertThat(dao.get(new NamespaceAndName("hitchhiker","HeartOfGold"))).isEqualTo(heartOfGold);
  }

  @Test
  void shouldNotReturnTheSameInstance() {
    Repository heartOfGold = createHeartOfGold();
    dao.add(heartOfGold);

    Repository repository = dao.get("42");
    assertThat(repository).isNotSameAs(heartOfGold);
  }

  @Test
  void shouldReturnAllRepositories() {
    Repository heartOfGold = createHeartOfGold();
    dao.add(heartOfGold);

    Repository puzzle = createPuzzle();
    dao.add(puzzle);

    Collection<Repository> repositories = dao.getAll();
    assertThat(repositories).containsExactlyInAnyOrder(heartOfGold, puzzle);
  }

  private Repository createPuzzle() {
    Repository puzzle = RepositoryTestData.create42Puzzle();
    puzzle.setId("42+1");
    return puzzle;
  }

  @Test
  void shouldModifyRepository() {
    Repository heartOfGold = createHeartOfGold();
    heartOfGold.setDescription("HeartOfGold");
    dao.add(heartOfGold);
    assertThat(dao.get("42").getDescription()).isEqualTo("HeartOfGold");

    heartOfGold = createHeartOfGold();
    heartOfGold.setDescription("Heart of Gold");
    dao.modify(heartOfGold);

    assertThat(dao.get("42").getDescription()).isEqualTo("Heart of Gold");
  }

  @Test
  void shouldRemoveRepository() {
    Repository heartOfGold = createHeartOfGold();
    dao.add(heartOfGold);
    assertThat(dao.contains("42")).isTrue();

    dao.delete(heartOfGold);
    assertThat(dao.contains("42")).isFalse();
    assertThat(dao.contains(new NamespaceAndName("hitchhiker", "HeartOfGold"))).isFalse();
  }

  @Test
  void shouldUpdateLastModifiedAfterEachWriteOperation() {
    Repository heartOfGold = createHeartOfGold();
    dao.add(heartOfGold);

    Long firstLastModified = dao.getLastModified();
    assertThat(firstLastModified).isNotNull();

    Repository puzzle = createPuzzle();
    dao.add(puzzle);

    Long lastModifiedAdded = dao.getLastModified();
    assertThat(lastModifiedAdded).isGreaterThan(firstLastModified);

    heartOfGold.setDescription("Heart of Gold");
    dao.modify(heartOfGold);

    Long lastModifiedModified = dao.getLastModified();
    assertThat(lastModifiedModified).isGreaterThan(lastModifiedAdded);

    dao.delete(puzzle);

    Long lastModifiedRemoved = dao.getLastModified();
    assertThat(lastModifiedRemoved).isGreaterThan(lastModifiedModified);
  }

  @Test
  void shouldRenameTheRepository() {
    Repository heartOfGold = createHeartOfGold();
    dao.add(heartOfGold);

    heartOfGold.setNamespace("hg2tg");
    heartOfGold.setName("hog");

    dao.modify(heartOfGold);

    Repository repository = dao.get("42");
    assertThat(repository.getNamespace()).isEqualTo("hg2tg");
    assertThat(repository.getName()).isEqualTo("hog");

    assertThat(dao.contains(new NamespaceAndName("hg2tg", "hog"))).isTrue();
    assertThat(dao.contains(new NamespaceAndName("hitchhiker", "HeartOfGold"))).isFalse();
  }

  @Test
  void shouldDeleteRepositoryEvenWithChangedNamespace() {
    Repository heartOfGold = createHeartOfGold();
    dao.add(heartOfGold);

    heartOfGold.setNamespace("hg2tg");
    heartOfGold.setName("hog");

    dao.delete(heartOfGold);

    assertThat(dao.contains(new NamespaceAndName("hitchhiker", "HeartOfGold"))).isFalse();
  }

  @Test
  void shouldReturnThePathForTheRepository() {
    Path repositoryPath = Paths.get("r", "42");
    when(locationResolver.getPath("42")).thenReturn(repositoryPath);

    Repository heartOfGold = createHeartOfGold();
    dao.add(heartOfGold);

    Path path = dao.getPath("42");
    assertThat(path).isEqualTo(repositoryPath);
  }

  @Test
  void shouldCreateTheDirectoryForTheRepository() {
    Path repositoryPath = Paths.get("r", "42");
    when(locationResolver.getPath("42")).thenReturn(repositoryPath);

    Repository heartOfGold = createHeartOfGold();
    dao.add(heartOfGold);

    Path path = getAbsolutePathFromDao("42");
    assertThat(path).isDirectory();
  }

  @Test
  void shouldRemoveRepositoryDirectoryAfterDeletion() {
    Repository heartOfGold = createHeartOfGold();
    dao.add(heartOfGold);

    Path path = getAbsolutePathFromDao(heartOfGold.getId());
    assertThat(path).isDirectory();

    dao.delete(heartOfGold);
    assertThat(path).doesNotExist();
  }

  private Path getAbsolutePathFromDao(String id) {
    return context.resolve(dao.getPath(id));
  }

  @Test
  void shouldCreateRepositoryPathDatabase() throws IOException {
    Repository heartOfGold = createHeartOfGold();
    dao.add(heartOfGold);

    Path storePath = dao.resolveStorePath();
    assertThat(storePath).isRegularFile();

    String content = content(storePath);

    assertThat(content).contains(heartOfGold.getId());
    assertThat(content).contains(dao.getPath(heartOfGold.getId()).toString());
  }

  private String content(Path storePath) throws IOException {
    return new String(Files.readAllBytes(storePath), Charsets.UTF_8);
  }

  @Test
  void shouldStoreRepositoryMetadataAfterAdd() throws IOException {
    Repository heartOfGold = createHeartOfGold();
    dao.add(heartOfGold);

    Path repositoryDirectory = getAbsolutePathFromDao(heartOfGold.getId());
    Path metadataPath = dao.resolveMetadataPath(repositoryDirectory);

    assertThat(metadataPath).isRegularFile();

    String content = content(metadataPath);
    assertThat(content).contains(heartOfGold.getName());
    assertThat(content).contains(heartOfGold.getNamespace());
    assertThat(content).contains(heartOfGold.getDescription());
  }

  @Test
  void shouldUpdateRepositoryMetadataAfterModify() throws IOException {
    Repository heartOfGold = createHeartOfGold();
    dao.add(heartOfGold);

    heartOfGold.setDescription("Awesome Spaceship");
    dao.modify(heartOfGold);

    Path repositoryDirectory = getAbsolutePathFromDao(heartOfGold.getId());
    Path metadataPath = dao.resolveMetadataPath(repositoryDirectory);

    String content = content(metadataPath);
    assertThat(content).contains("Awesome Spaceship");
  }

  @Test
  void shouldPersistPermissions() throws IOException {
    Repository heartOfGold = createHeartOfGold();
    heartOfGold.setPermissions(asList(new RepositoryPermission("trillian", asList("read", "write"), false), new RepositoryPermission("vogons", Collections.singletonList("delete"), true)));
    dao.add(heartOfGold);

    Path repositoryDirectory = getAbsolutePathFromDao(heartOfGold.getId());
    Path metadataPath = dao.resolveMetadataPath(repositoryDirectory);

    String content = content(metadataPath);
    System.out.println(content);
    assertThat(content).containsSubsequence("trillian", "<verb>read</verb>", "<verb>write</verb>");
    assertThat(content).containsSubsequence("vogons", "<verb>delete</verb>");
  }

  @Test
  void shouldReadPathDatabaseAndMetadataOfRepositories() {
    Repository heartOfGold = createHeartOfGold();
    dao.add(heartOfGold);

    // reload data
    dao = createDAO();

    heartOfGold = dao.get("42");
    assertThat(heartOfGold.getName()).isEqualTo("HeartOfGold");

    Path path = getAbsolutePathFromDao(heartOfGold.getId());
    assertThat(path).isDirectory();
  }

  @Test
  void shouldReadCreationTimeAndLastModifedDateFromDatabase() {
    Repository heartOfGold = createHeartOfGold();
    dao.add(heartOfGold);

    Long creationTime = dao.getCreationTime();
    Long lastModified = dao.getLastModified();

    // reload data
    dao = createDAO();

    assertThat(dao.getCreationTime()).isEqualTo(creationTime);
    assertThat(dao.getLastModified()).isEqualTo(lastModified);
  }
}
