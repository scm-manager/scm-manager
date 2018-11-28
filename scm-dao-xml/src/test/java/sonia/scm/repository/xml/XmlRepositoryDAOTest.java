package sonia.scm.repository.xml;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.SCMContextProvider;
import sonia.scm.io.DefaultFileSystem;
import sonia.scm.io.FileSystem;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.StoreParameters;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.repository.xml.XmlRepositoryDAO.STORE_NAME;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class XmlRepositoryDAOTest {

  @Mock
  private ConfigurationStoreFactory storeFactory;
  @Mock
  private ConfigurationStore<XmlRepositoryDatabase> store;
  @Mock
  private XmlRepositoryDatabase db;
  @Mock
  private SCMContextProvider context;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private final FileSystem fileSystem = new DefaultFileSystem();

  @Before
  public void init() throws IOException {
    StoreParameters storeParameters = new StoreParameters().withType(XmlRepositoryDatabase.class).withName(STORE_NAME).build();
    when(storeFactory.getStore(storeParameters)).thenReturn(store);
    when(store.get()).thenReturn(db);
    when(context.getBaseDirectory()).thenReturn(temporaryFolder.newFolder());
  }

  @Test
  public void addShouldCreateNewRepositoryPathWithRelativePath() {
    InitialRepositoryLocationResolver initialRepositoryLocationResolver = new InitialRepositoryLocationResolver(context);
    XmlRepositoryDAO dao = new XmlRepositoryDAO(initialRepositoryLocationResolver, fileSystem, context);

    dao.add(new Repository("id", "git", "namespace", "name"));

    verify(db).add(argThat(repositoryPath -> {
      assertThat(repositoryPath.getId()).isEqualTo("id");
      assertThat(repositoryPath.getPath()).isEqualTo(InitialRepositoryLocationResolver.DEFAULT_REPOSITORY_PATH + "/id");
      return true;
    }));
    verify(store).set(db);
  }

  @Test
  public void modifyShouldStoreChangedRepository() {
    Repository oldRepository = new Repository("id", "old", null, null);
    RepositoryPath repositoryPath = new RepositoryPath("/path", "id", oldRepository);
    when(db.values()).thenReturn(asList(repositoryPath));

    XmlRepositoryDAO dao = new XmlRepositoryDAO(new InitialRepositoryLocationResolver(context), fileSystem, context);

    Repository newRepository = new Repository("id", "new", null, null);
    dao.modify(newRepository);

    assertThat(repositoryPath.getRepository()).isSameAs(newRepository);
    verify(store).set(db);
  }

  @Test
  public void shouldGetPathInBaseDirectoryForRelativePath() {
    Repository existingRepository = new Repository("id", "old", null, null);
    RepositoryPath repositoryPath = new RepositoryPath("path", "id", existingRepository);
    when(db.values()).thenReturn(asList(repositoryPath));

    XmlRepositoryDAO dao = new XmlRepositoryDAO(new InitialRepositoryLocationResolver(context), fileSystem, context);

    Path path = dao.getPath(existingRepository);

    assertThat(path.toString()).isEqualTo(context.getBaseDirectory().getPath() + "/path");
  }

  @Test
  public void shouldGetPathInBaseDirectoryForAbsolutePath() {
    Repository existingRepository = new Repository("id", "old", null, null);
    RepositoryPath repositoryPath = new RepositoryPath("/tmp/path", "id", existingRepository);
    when(db.values()).thenReturn(asList(repositoryPath));

    XmlRepositoryDAO dao = new XmlRepositoryDAO(new InitialRepositoryLocationResolver(context), fileSystem, context);

    Path path = dao.getPath(existingRepository);

    assertThat(path.toString()).isEqualTo("/tmp/path");
  }
}
