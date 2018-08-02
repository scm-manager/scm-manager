/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */
package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.github.legman.Subscribe;
import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.shiro.authz.UnauthorizedException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import sonia.scm.HandlerEventType;
import sonia.scm.Manager;
import sonia.scm.ManagerTestBase;
import sonia.scm.ModelObject;
import sonia.scm.Type;
import sonia.scm.TypedObject;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.spi.HookContextProvider;
import sonia.scm.repository.xml.XmlRepositoryDAO;
import sonia.scm.security.DefaultKeyGenerator;
import sonia.scm.security.KeyGenerator;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.JAXBConfigurationStoreFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

//~--- JDK imports ------------------------------------------------------------

/**
 * Unit tests for {@link DefaultRepositoryManager}.
 * 
 * @author Sebastian Sdorra
 */
@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class DefaultRepositoryManagerTest extends ManagerTestBase<Repository, RepositoryException> {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private ScmConfiguration configuration;

  private String mockedNamespace = "default_namespace";

  @Test
  public void testCreate() throws RepositoryException {
    Repository heartOfGold = createTestRepository();
    Repository dbRepo = manager.get(heartOfGold.getId());

    assertNotNull(dbRepo);
    assertRepositoriesEquals(dbRepo, heartOfGold);
  }

  @SubjectAware(
    username = "unpriv"
  )
  @Test(expected = UnauthorizedException.class)
  public void testCreateWithoutPrivileges() throws RepositoryException {
    createTestRepository();
  }

  @Test(expected = RepositoryAlreadyExistsException.class)
  public void testCreateExisting() throws RepositoryException {
    createTestRepository();
    createTestRepository();
  }

  @Test
  public void testDelete() throws RepositoryException {
    delete(manager, createTestRepository());
  }

  @SubjectAware(
    username = "unpriv"
  )
  @Test(expected = UnauthorizedException.class)
  public void testDeleteWithoutPrivileges() throws RepositoryException {
    delete(manager, createTestRepository());
  }

  @Test(expected = RepositoryIsNotArchivedException.class)
  public void testDeleteNonArchived() throws RepositoryException {
    configuration.setEnableRepositoryArchive(true);
    delete(manager, createTestRepository());
  }

  @Test(expected = RepositoryNotFoundException.class)
  public void testDeleteNotFound() throws RepositoryException {
    manager.delete(createRepositoryWithId());
  }

  @Test
  public void testDeleteWithEnabledArchive()
    throws RepositoryException {
    Repository repository = createTestRepository();

    repository.setArchived(true);
    RepositoryManager drm = createRepositoryManager(true);
    drm.init(contextProvider);
    delete(drm, repository);
  }

  @Test
  public void testGet() throws RepositoryException {
    Repository heartOfGold = createTestRepository();
    String id = heartOfGold.getId();
    String description = heartOfGold.getDescription();

    assertNotNull(description);

    // test for reference
    heartOfGold.setDescription("prototype ship");
    heartOfGold = manager.get(id);
    assertNotNull(heartOfGold);
    assertEquals(description, heartOfGold.getDescription());
  }

  @Test
  @SubjectAware(
    username = "crato"
  )
  public void testGetWithoutRequiredPrivileges() throws RepositoryException {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    manager.create(heartOfGold);

    thrown.expect(UnauthorizedException.class);
    manager.get(heartOfGold.getId());
  }

  @Test
  public void testGetAll() throws RepositoryException {
    Repository heartOfGold = createTestRepository();
    Repository happyVerticalPeopleTransporter = createSecondTestRepository();
    boolean foundHeart = false;
    boolean foundTransporter = false;
    Collection<Repository> repositories = manager.getAll();

    assertNotNull(repositories);
    assertFalse(repositories.isEmpty());
    assertTrue(repositories.size() >= 2);

    Repository heartReference = null;

    for (Repository repository : repositories) {
      if (repository.getId().equals(heartOfGold.getId())) {
        assertRepositoriesEquals(heartOfGold, repository);
        foundHeart = true;
        heartReference = repository;
      }
      else if (repository.getId().equals(happyVerticalPeopleTransporter.getId())) {
        assertRepositoriesEquals(happyVerticalPeopleTransporter, repository);
        foundTransporter = true;
      }
    }

    assertTrue(foundHeart);
    assertTrue(foundTransporter);

    // test for reference
    assertNotSame(heartOfGold, heartReference);
    heartReference.setDescription("prototype ship");
    assertFalse(
      heartOfGold.getDescription().equals(heartReference.getDescription()));
  }

  @Test
  @SuppressWarnings("unchecked")
  @SubjectAware(username = "dent")
  public void testGetAllWithPermissionsForTwoOrThreeRepos() throws RepositoryException {
    // mock key generator
    KeyGenerator keyGenerator = mock(KeyGenerator.class);
    Stack<String> keys = new Stack<>();
    keys.push("rateotu");
    keys.push("p42");
    keys.push("hof");

    when(keyGenerator.createKey()).then((InvocationOnMock invocation) -> {
      return keys.pop();
    });

    // create repository manager
    RepositoryManager repositoryManager = createRepositoryManager(false, keyGenerator);

    // create first test repository
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    repositoryManager.create(heartOfGold);
    assertEquals("hof", heartOfGold.getId());

    // create second test repository
    Repository puzzle42 = RepositoryTestData.create42Puzzle();
    repositoryManager.create(puzzle42);
    assertEquals("p42", puzzle42.getId());

    // create third test repository
    Repository restaurant = RepositoryTestData.createRestaurantAtTheEndOfTheUniverse();
    repositoryManager.create(restaurant);
    assertEquals("rateotu", restaurant.getId());

    // assert returned repositories
    Collection<Repository> repositories = repositoryManager.getAll();
    assertEquals(2, repositories.size());
    assertThat(repositories, containsInAnyOrder(
        hasProperty("id", is("p42")),
        hasProperty("id", is("hof"))
      )
    );
  }

  @Test
  public void testEvents() throws RepositoryException {
    RepositoryManager repoManager = createRepositoryManager(false);
    repoManager.init(contextProvider);
    TestListener listener = new TestListener();

    ScmEventBus.getInstance().register(listener);

    Repository repository = RepositoryTestData.create42Puzzle();

    repoManager.create(repository);
    assertRepositoriesEquals(repository, listener.preRepository);
    assertSame(HandlerEventType.BEFORE_CREATE, listener.preEvent);
    assertRepositoriesEquals(repository, listener.postRepository);
    assertSame(HandlerEventType.CREATE, listener.postEvent);

    repository.setDescription("changed description");
    repoManager.modify(repository);
    assertRepositoriesEquals(repository, listener.preRepository);
    assertSame(HandlerEventType.BEFORE_MODIFY, listener.preEvent);
    assertRepositoriesEquals(repository, listener.postRepository);
    assertSame(HandlerEventType.MODIFY, listener.postEvent);

    repoManager.delete(repository);

    assertRepositoriesEquals(repository, listener.preRepository);
    assertSame(HandlerEventType.BEFORE_DELETE, listener.preEvent);
    assertRepositoriesEquals(repository, listener.postRepository);
    assertSame(HandlerEventType.DELETE, listener.postEvent);
  }

  @Test
  public void testModify() throws RepositoryException {
    Repository heartOfGold = createTestRepository();

    heartOfGold.setDescription("prototype ship");
    manager.modify(heartOfGold);

    Repository hearReference = manager.get(heartOfGold.getId());

    assertNotNull(hearReference);
    assertEquals(hearReference.getDescription(), "prototype ship");
  }

  @Test
  @SubjectAware(username = "crato")
  public void testModifyWithoutRequiredPermissions() throws RepositoryException {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    manager.create(heartOfGold);
    heartOfGold.setDescription("prototype ship");
    
    thrown.expect(UnauthorizedException.class);
    manager.modify(heartOfGold);
  }

  @Test(expected = RepositoryNotFoundException.class)
  public void testModifyNotFound() throws RepositoryException {
    manager.modify(createRepositoryWithId());
  }

  @Test
  public void testRefresh() throws RepositoryException {
    Repository heartOfGold = createTestRepository();
    String description = heartOfGold.getDescription();

    heartOfGold.setDescription("prototype ship");
    manager.refresh(heartOfGold);
    assertEquals(description, heartOfGold.getDescription());
  }

  @Test
  @SubjectAware(username = "crato")
  public void testRefreshWithoutRequiredPermissions() throws RepositoryException {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    manager.create(heartOfGold);
    heartOfGold.setDescription("prototype ship");
    
    thrown.expect(UnauthorizedException.class);
    manager.refresh(heartOfGold);
  }

  @Test(expected = RepositoryNotFoundException.class)
  public void testRefreshNotFound() throws RepositoryException {
    manager.refresh(createRepositoryWithId());
  }

  @Test
  public void testRepositoryHook() throws RepositoryException {
    CountingReceiveHook hook = new CountingReceiveHook();
    RepositoryManager repoManager = createRepositoryManager(false);

    ScmEventBus.getInstance().register(hook);

    assertEquals(0, hook.eventsReceived);

    Repository repository = createTestRepository();
    HookContext ctx = createHookContext(repository);

    repoManager.fireHookEvent(new RepositoryHookEvent(ctx, repository,
      RepositoryHookType.POST_RECEIVE));
    assertEquals(1, hook.eventsReceived);
    repoManager.fireHookEvent(new RepositoryHookEvent(ctx, repository,
      RepositoryHookType.POST_RECEIVE));
    assertEquals(2, hook.eventsReceived);
  }

  @Test
  public void testNamespaceSet() throws Exception {
    RepositoryManager repoManager = createRepositoryManager(false);
    Repository repository = spy(createTestRepository());
    repository.setName("Testrepo");
    ((DefaultRepositoryManager) repoManager).create(repository);
    assertEquals("default_namespace", repository.getNamespace());
  }

  @Test
  public void getRepositoryFromRequestUri_withoutLeadingSlash() throws RepositoryException {
    RepositoryManager m = createManager();
    m.init(contextProvider);

    createUriTestRepositories(m);

    assertEquals("scm-test", m.getFromUri("hg/namespace/scm-test").getName());
    assertEquals("namespace", m.getFromUri("hg/namespace/scm-test").getNamespace());
  }

  @Test
  public void getRepositoryFromRequestUri_withLeadingSlash() throws RepositoryException {
    RepositoryManager m = createManager();
    m.init(contextProvider);

    createUriTestRepositories(m);

    assertEquals("scm-test", m.getFromUri("/hg/namespace/scm-test").getName());
    assertEquals("namespace", m.getFromUri("/hg/namespace/scm-test").getNamespace());
  }

  @Test
  public void getRepositoryFromRequestUri_withPartialName() throws RepositoryException {
    RepositoryManager m = createManager();
    m.init(contextProvider);

    createUriTestRepositories(m);

    assertEquals("scm", m.getFromUri("hg/namespace/scm").getName());
    assertEquals("namespace", m.getFromUri("hg/namespace/scm").getNamespace());
  }

  @Test
  public void getRepositoryFromRequestUri_withTrailingFilePath() throws RepositoryException {
    RepositoryManager m = createManager();
    m.init(contextProvider);

    createUriTestRepositories(m);

    assertEquals("test-1", m.getFromUri("/git/namespace/test-1/ka/some/path").getName());
  }

  @Test
  public void getRepositoryFromRequestUri_forNotExistingRepositoryName() throws RepositoryException {
    RepositoryManager m = createManager();
    m.init(contextProvider);

    createUriTestRepositories(m);

    assertNull(m.getFromUri("/git/namespace/test-3/ka/some/path"));
  }

  @Test
  public void getRepositoryFromRequestUri_forWrongNamespace() throws RepositoryException {
    RepositoryManager m = createManager();
    m.init(contextProvider);

    createUriTestRepositories(m);

    assertNull(m.getFromUri("/git/other/other/test-2"));
  }

  @Test
  public void shouldSetNamespace() throws RepositoryException {
    Repository repository = new Repository(null, "hg", null, "scm");
    manager.create(repository);
    assertNotNull(repository.getId());
    assertNotNull(repository.getNamespace());
  }

  private void createUriTestRepositories(RepositoryManager m) throws RepositoryException {
    mockedNamespace = "namespace";
    createRepository(m, new Repository("1", "hg", "namespace", "scm"));
    createRepository(m, new Repository("2", "hg", "namespace", "scm-test"));
    createRepository(m, new Repository("3", "git", "namespace", "test-1"));
    createRepository(m, new Repository("4", "git", "namespace", "test-2"));

    mockedNamespace = "other";
    createRepository(m, new Repository("1", "hg", "other", "scm"));
    createRepository(m, new Repository("2", "hg", "other", "scm-test"));
  }

  //~--- methods --------------------------------------------------------------
  
  @Override
  protected DefaultRepositoryManager createManager() {
    return createRepositoryManager(false);
  }

  private DefaultRepositoryManager createRepositoryManager(boolean archiveEnabled) {
    return createRepositoryManager(archiveEnabled, new DefaultKeyGenerator());
  }

  private DefaultRepositoryManager createRepositoryManager(boolean archiveEnabled, KeyGenerator keyGenerator) {
    Set<RepositoryHandler> handlerSet = new HashSet<>();
    ConfigurationStoreFactory factory = new JAXBConfigurationStoreFactory(contextProvider);
    handlerSet.add(new DummyRepositoryHandler(factory));
    handlerSet.add(new DummyRepositoryHandler(factory) {
      @Override
      public RepositoryType getType() {
        return new RepositoryType("hg", "Mercurial", Sets.newHashSet());
      }
    });
    handlerSet.add(new DummyRepositoryHandler(factory) {
      @Override
      public RepositoryType getType() {
        return new RepositoryType("git", "Git", Sets.newHashSet());
      }
    });

    XmlRepositoryDAO repositoryDAO = new XmlRepositoryDAO(factory);

    this.configuration = new ScmConfiguration();

    configuration.setEnableRepositoryArchive(archiveEnabled);

    NamespaceStrategy namespaceStrategy = mock(NamespaceStrategy.class);
    when(namespaceStrategy.createNamespace(Mockito.any(Repository.class))).thenAnswer(invocation -> mockedNamespace);

    return new DefaultRepositoryManager(configuration, contextProvider,
      keyGenerator, repositoryDAO, handlerSet, createRepositoryMatcher(), namespaceStrategy);
  }
  
  private void createRepository(RepositoryManager m, Repository repository)
    throws RepositoryException {
    m.create(repository);
  }

  private HookContext createHookContext(Repository repository) {
    PreProcessorUtil ppu = mock(PreProcessorUtil.class);
    HookContextProvider provider = mock(HookContextProvider.class);
    Set<HookFeature> features = ImmutableSet.of();

    when(provider.getSupportedFeatures()).thenReturn(features);

    return new HookContextFactory(ppu).createContext(provider, repository);
  }
  
  private void assertRepositoriesEquals(Repository repo, Repository other) {
    assertEquals(repo.getId(), other.getId());
    assertEquals(repo.getName(), other.getName());
    assertEquals(repo.getDescription(), other.getDescription());
    assertEquals(repo.getContact(), other.getContact());
    assertEquals(repo.getCreationDate(), other.getCreationDate());
    assertEquals(repo.getLastModified(), other.getLastModified());
  }
          
  private RepositoryMatcher createRepositoryMatcher() {
    return new RepositoryMatcher(Collections.<RepositoryPathMatcher>emptySet());
  }       

  private Repository createRepository(Repository repository) throws RepositoryException {
    manager.create(repository);
    assertNotNull(repository.getId());
    assertNotNull(manager.get(repository.getId()));
    assertTrue(repository.getCreationDate() > 0);

    return repository;
  }

  private Repository createRepositoryWithId() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    repository.setId("abc");
    return repository;
  }

  private Repository createSecondTestRepository() throws RepositoryException {
    return createRepository(
      RepositoryTestData.createHappyVerticalPeopleTransporter());
  }

  private Repository createTestRepository() throws RepositoryException {
    return createRepository(RepositoryTestData.createHeartOfGold());
  }

  private void delete(Manager<Repository, RepositoryException> manager, Repository repository)
    throws RepositoryException {

    String id = repository.getId();

    manager.delete(repository);
    assertNull(manager.get(id));
  }

  private static class CountingReceiveHook {

    private int eventsReceived = 0;

    @Subscribe(async = false)
    public void onEvent(PostReceiveRepositoryHookEvent event) {
      eventsReceived++;
    }

    @Subscribe(async = false)
    public void onEvent(PreReceiveRepositoryHookEvent event) {
      eventsReceived++;
    }
  }

  private class TestListener {

    private HandlerEventType postEvent;

    private Repository postRepository;

    private HandlerEventType preEvent;

    private Repository preRepository;

    @Subscribe(async = false)
    public void onEvent(RepositoryEvent event) {
      if (event.getEventType().isPost()) {
        this.postRepository = event.getItem();
        this.postEvent = event.getEventType();
      }
      else if (event.getEventType().isPre()) {
        this.preRepository = event.getItem();
        this.preEvent = event.getEventType();
      }
    }
  }

}
