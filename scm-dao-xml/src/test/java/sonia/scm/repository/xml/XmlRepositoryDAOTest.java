package sonia.scm.repository.xml;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.SCMContextProvider;
import sonia.scm.io.DefaultFileSystem;
import sonia.scm.io.FileSystem;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.util.IOUtil;
import sun.misc.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.BiConsumer;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static sonia.scm.repository.RepositoryTestData.createHeartOfGold;

@ExtendWith({MockitoExtension.class, TempDirectory.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class XmlRepositoryDAOTest {

  private final Repository REPOSITORY = createRepository("42");

  @Mock
  private PathBasedRepositoryLocationResolver locationResolver;

  @Captor
  private ArgumentCaptor<BiConsumer<String, Path>> forAllCaptor;

  private FileSystem fileSystem = new DefaultFileSystem();

  private XmlRepositoryDAO dao;

  @BeforeEach
  void createDAO(@TempDirectory.TempDir Path basePath) {
    when(locationResolver.create(Path.class)).thenReturn(locationResolver::create);
    when(locationResolver.create(anyString())).thenAnswer(invocation -> createMockedRepoPath(basePath, invocation));
    when(locationResolver.remove(anyString())).thenAnswer(invocation -> basePath.resolve(invocation.getArgument(0).toString()));
  }

  private Path createMockedRepoPath(@TempDirectory.TempDir Path basePath, InvocationOnMock invocation) {
    Path resolvedPath = basePath.resolve(invocation.getArgument(0).toString());
    try {
      Files.createDirectories(resolvedPath);
    } catch (IOException e) {
      fail(e);
    }
    return resolvedPath;
  }

  @Nested
  class WithEmptyDatabase {

    @BeforeEach
    void createDAO() {
      dao = new XmlRepositoryDAO(locationResolver, fileSystem);
    }

    @Test
    void shouldReturnXmlType() {
      assertThat(dao.getType()).isEqualTo("xml");
    }

    @Test
    void shouldReturnCreationTimeOfLocationResolver() {
      long now = 42L;
      when(locationResolver.getCreationTime()).thenReturn(now);
      assertThat(dao.getCreationTime()).isEqualTo(now);
    }

    @Test
    void shouldReturnLasModifiedOfLocationResolver() {
      long now = 42L;
      when(locationResolver.getLastModified()).thenReturn(now);
      assertThat(dao.getLastModified()).isEqualTo(now);
    }

    @Test
    void shouldReturnTrueForEachContainsMethod() {
      dao.add(REPOSITORY);

      assertThat(dao.contains(REPOSITORY)).isTrue();
      assertThat(dao.contains(REPOSITORY.getId())).isTrue();
      assertThat(dao.contains(REPOSITORY.getNamespaceAndName())).isTrue();
    }

    @Test
    void shouldPersistRepository() {
      dao.add(REPOSITORY);

      String content = getXmlFileContent(REPOSITORY.getId());

      assertThat(content).contains("<id>42</id>");
    }

    @Test
    void shouldDeleteDataFile() {
      dao.add(REPOSITORY);
      dao.delete(REPOSITORY);

      assertThat(metadataFile(REPOSITORY.getId())).doesNotExist();
    }

    @Test
    void shouldModifyRepository() {
      dao.add(REPOSITORY);
      Repository changedRepository = REPOSITORY.clone();
      changedRepository.setContact("change");

      dao.modify(changedRepository);

      String content = getXmlFileContent(REPOSITORY.getId());

      assertThat(content).contains("change");
    }

    @Test
    void shouldReturnFalseForEachContainsMethod() {
      assertThat(dao.contains(REPOSITORY)).isFalse();
      assertThat(dao.contains(REPOSITORY.getId())).isFalse();
      assertThat(dao.contains(REPOSITORY.getNamespaceAndName())).isFalse();
    }

    @Test
    void shouldReturnNullForEachGetMethod() {
      assertThat(dao.get("42")).isNull();
      assertThat(dao.get(new NamespaceAndName("hitchhiker", "HeartOfGold"))).isNull();
    }

    @Test
    void shouldReturnRepository() {
      dao.add(REPOSITORY);

      assertThat(dao.get("42")).isEqualTo(REPOSITORY);
      assertThat(dao.get(new NamespaceAndName("space", "42"))).isEqualTo(REPOSITORY);
    }

    @Test
    void shouldNotReturnTheSameInstance() {
      dao.add(REPOSITORY);

      Repository repository = dao.get("42");
      assertThat(repository).isNotSameAs(REPOSITORY);
    }

    @Test
    void shouldReturnAllRepositories() {
      dao.add(REPOSITORY);

      Repository secondRepository = createRepository("23");
      dao.add(secondRepository);

      Collection<Repository> repositories = dao.getAll();
      assertThat(repositories)
        .containsExactlyInAnyOrder(REPOSITORY, secondRepository);
    }

    @Test
    void shouldModifyRepositoryTwice() {
      REPOSITORY.setDescription("HeartOfGold");
      dao.add(REPOSITORY);
      assertThat(dao.get("42").getDescription()).isEqualTo("HeartOfGold");

      Repository heartOfGold = createRepository("42");
      heartOfGold.setDescription("Heart of Gold");
      dao.modify(heartOfGold);

      assertThat(dao.get("42").getDescription()).isEqualTo("Heart of Gold");
    }

    @Test
    void shouldRemoveRepository() {
      dao.add(REPOSITORY);
      assertThat(dao.contains("42")).isTrue();

      dao.delete(REPOSITORY);
      assertThat(dao.contains("42")).isFalse();
      assertThat(dao.contains(REPOSITORY.getNamespaceAndName())).isFalse();

      Path storePath = metadataFile(REPOSITORY.getId());

      assertThat(storePath).doesNotExist();
    }

    @Test
    void shouldRenameTheRepository() {
      dao.add(REPOSITORY);

      REPOSITORY.setNamespace("hg2tg");
      REPOSITORY.setName("hog");

      dao.modify(REPOSITORY);

      Repository repository = dao.get("42");
      assertThat(repository.getNamespace()).isEqualTo("hg2tg");
      assertThat(repository.getName()).isEqualTo("hog");

      assertThat(dao.contains(new NamespaceAndName("hg2tg", "hog"))).isTrue();
      assertThat(dao.contains(new NamespaceAndName("hitchhiker", "HeartOfGold"))).isFalse();

      String content = getXmlFileContent(REPOSITORY.getId());
      assertThat(content).contains("<name>hog</name>");
    }

    @Test
    void shouldDeleteRepositoryEvenWithChangedNamespace() {
      dao.add(REPOSITORY);

      REPOSITORY.setNamespace("hg2tg");
      REPOSITORY.setName("hog");

      dao.delete(REPOSITORY);

      assertThat(dao.contains(new NamespaceAndName("space", "42"))).isFalse();
    }

    @Test
    void shouldRemoveRepositoryDirectoryAfterDeletion() {
      dao.add(REPOSITORY);

      Path path = locationResolver.create(REPOSITORY.getId());
      assertThat(path).isDirectory();

      dao.delete(REPOSITORY);
      assertThat(path).doesNotExist();
    }

    @Test
    void shouldPersistPermissions() {
      REPOSITORY.setPermissions(asList(new RepositoryPermission("trillian", asList("read", "write"), false), new RepositoryPermission("vogons", singletonList("delete"), true)));
      dao.add(REPOSITORY);

      String content = getXmlFileContent(REPOSITORY.getId());
      System.out.println(content);
      assertThat(content).containsSubsequence("trillian", "<verb>read</verb>", "<verb>write</verb>");
      assertThat(content).containsSubsequence("vogons", "<verb>delete</verb>");
    }
  }

  @Test
  void shouldReadExistingRepositoriesFromPathDatabase(@TempDirectory.TempDir Path basePath) throws IOException {
    doNothing().when(locationResolver).forAllPaths(forAllCaptor.capture());
    XmlRepositoryDAO dao = new XmlRepositoryDAO(locationResolver, fileSystem);

    Path repositoryPath = basePath.resolve("existing");
    Files.createDirectories(repositoryPath);
    URL metadataUrl = Resources.getResource("sonia/scm/store/repositoryDaoMetadata.xml");
    Files.copy(metadataUrl.openStream(), repositoryPath.resolve("metadata.xml"));

    forAllCaptor.getValue().accept("existing", repositoryPath);

    assertThat(dao.contains(new NamespaceAndName("space", "existing"))).isTrue();
  }

  private String getXmlFileContent(String id) {
    Path storePath = metadataFile(id);

    assertThat(storePath).isRegularFile();
    return content(storePath);
  }

  private Path metadataFile(String id) {
    return locationResolver.create(id).resolve("metadata.xml");
  }

  private String content(Path storePath) {
    try {
      return new String(Files.readAllBytes(storePath), Charsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Repository createRepository(String id) {
    return new Repository(id, "xml", "space", id);
  }
}
