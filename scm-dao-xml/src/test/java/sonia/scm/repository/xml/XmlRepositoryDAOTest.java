package sonia.scm.repository.xml;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.repository.xml.XmlRepositoryDAO.STORE_NAME;

@RunWith(MockitoJUnitRunner.class)
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

  @Before
  public void init() throws IOException {
    when(storeFactory.getStore(XmlRepositoryDatabase.class, STORE_NAME)).thenReturn(store);
    when(store.get()).thenReturn(db);
    when(context.getBaseDirectory()).thenReturn(temporaryFolder.newFolder());
  }

  @Test
  public void addShouldCreateNewRepositoryPathWithRelativePath() {
    InitialRepositoryLocationResolver initialRepositoryLocationResolver = new InitialRepositoryLocationResolver(context);
    XmlRepositoryDAO dao = new XmlRepositoryDAO(storeFactory, initialRepositoryLocationResolver, context);

    dao.add(new Repository("id", null, null, null));

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
    when(db.getPaths()).thenReturn(asList(repositoryPath));

    XmlRepositoryDAO dao = new XmlRepositoryDAO(storeFactory, new InitialRepositoryLocationResolver(context), context);

    Repository newRepository = new Repository("id", "new", null, null);
    dao.modify(newRepository);

    assertThat(repositoryPath.getRepository()).isSameAs(newRepository);
    verify(store).set(db);
  }

  @Test
  public void shouldGetPathInBaseDirectoryForRelativePath() {
    Repository existingRepository = new Repository("id", "old", null, null);
    RepositoryPath repositoryPath = new RepositoryPath("path", "id", existingRepository);
    when(db.getPaths()).thenReturn(asList(repositoryPath));

    XmlRepositoryDAO dao = new XmlRepositoryDAO(storeFactory, new InitialRepositoryLocationResolver(context), context);

    Path path = dao.getPath(existingRepository);

    assertThat(path.toString()).isEqualTo(context.getBaseDirectory().getPath() + "/path");
  }

  @Test
  public void shouldGetPathInBaseDirectoryForAbsolutePath() {
    Repository existingRepository = new Repository("id", "old", null, null);
    RepositoryPath repositoryPath = new RepositoryPath("/tmp/path", "id", existingRepository);
    when(db.getPaths()).thenReturn(asList(repositoryPath));

    XmlRepositoryDAO dao = new XmlRepositoryDAO(storeFactory, new InitialRepositoryLocationResolver(context), context);

    Path path = dao.getPath(existingRepository);

    assertThat(path.toString()).isEqualTo("/tmp/path");
  }

  @Test
  public void shouldGetPathForNewRepository() {
    when(db.getPaths()).thenReturn(emptyList());

    InitialRepositoryLocationResolver initialRepositoryLocationResolver = new InitialRepositoryLocationResolver(context);
    XmlRepositoryDAO dao = new XmlRepositoryDAO(storeFactory, initialRepositoryLocationResolver, context);

    Repository newRepository = new Repository("id", "new", null, null);
    Path path = dao.getPath(newRepository);

    assertThat(path.toString()).isEqualTo(context.getBaseDirectory().getPath() + "/" + InitialRepositoryLocationResolver.DEFAULT_REPOSITORY_PATH + "/id");
  }

  @Test
  public void shouldFindRepositoryForRelativePath() {
    Repository existingRepository = new Repository("id", "old", null, null);
    RepositoryPath repositoryPath = new RepositoryPath("relative/path", "id", existingRepository);
    when(db.getPaths()).thenReturn(asList(repositoryPath));

    XmlRepositoryDAO dao = new XmlRepositoryDAO(storeFactory, new InitialRepositoryLocationResolver(context), context);

    String id = dao.getIdForDirectory(new File(context.getBaseDirectory(), "relative/path/data"));

    assertThat(id).isEqualTo("id");
  }

  @Test
  public void shouldFindRepositoryForAbsolutePath() {
    Repository existingRepository = new Repository("id", "old", null, null);
    RepositoryPath repositoryPath = new RepositoryPath("/tmp/somewhere/else", "id", existingRepository);
    when(db.getPaths()).thenReturn(asList(repositoryPath));

    XmlRepositoryDAO dao = new XmlRepositoryDAO(storeFactory, new InitialRepositoryLocationResolver(context), context);

    String id = dao.getIdForDirectory(new File("/tmp/somewhere/else/data"));

    assertThat(id).isEqualTo("id");
  }
}
