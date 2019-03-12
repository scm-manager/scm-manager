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
import com.google.inject.util.Providers;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import sonia.scm.AlreadyExistsException;
import sonia.scm.HandlerEventType;
import sonia.scm.Manager;
import sonia.scm.ManagerTestBase;
import sonia.scm.NotFoundException;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.event.ScmEventBus;
import sonia.scm.io.DefaultFileSystem;
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
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
public class DefaultRepositoryManagerTest extends ManagerTestBase<Repository> {

  {
    ThreadContext.unbindSubject();
  }

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private ScmConfiguration configuration;

  private String mockedNamespace = "default_namespace";

  @Before
  public void initContext() {
    ((TempSCMContextProvider)SCMContext.getContext()).setBaseDirectory(temp);
  }

  @Test
  public void testCreate() {
    Repository heartOfGold = createTestRepository();
    Repository dbRepo = manager.get(heartOfGold.getId());

    assertNotNull(dbRepo);
    assertRepositoriesEquals(dbRepo, heartOfGold);
  }

  @SubjectAware(
    username = "unpriv"
  )
  @Test(expected = UnauthorizedException.class)
  public void testCreateWithoutPrivileges() {
    createTestRepository();
  }

  @Test
  public void testCreateExisting() {
    Repository testRepository = createTestRepository();
    String expectedNamespaceAndName = testRepository.getNamespaceAndName().logString();
    thrown.expect(AlreadyExistsException.class);
    thrown.expectMessage(expectedNamespaceAndName);
    createTestRepository();
  }

  @Test
  public void testDelete() {
    delete(manager, createTestRepository());
  }

  @SubjectAware(
    username = "unpriv"
  )
  @Test(expected = UnauthorizedException.class)
  public void testDeleteWithoutPrivileges() {
    delete(manager, createTestRepository());
  }

  @Test(expected = RepositoryIsNotArchivedException.class)
  public void testDeleteNonArchived() {
    configuration.setEnableRepositoryArchive(true);
    delete(manager, createTestRepository());
  }

  @Test(expected = NotFoundException.class)
  public void testDeleteNotFound(){
    manager.delete(createRepositoryWithId());
  }

  @Test
  public void testDeleteWithEnabledArchive() {
    Repository repository = createTestRepository();

    repository.setArchived(true);
    RepositoryManager drm = createRepositoryManager(true);
    drm.init(contextProvider);
    delete(drm, repository);
  }

  @Test
  public void testGet() {
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
  public void testGetWithoutRequiredPrivileges() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    manager.create(heartOfGold);

    thrown.expect(UnauthorizedException.class);
    manager.get(heartOfGold.getId());
  }

  @Test
  public void testGetAll() {
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
  public void testGetAllWithPermissionsForTwoOrThreeRepos() {
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
  public void testEvents() {
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
  public void testModify() {
    Repository heartOfGold = createTestRepository();

    heartOfGold.setDescription("prototype ship");
    manager.modify(heartOfGold);

    Repository hearReference = manager.get(heartOfGold.getId());

    assertNotNull(hearReference);
    assertEquals(hearReference.getDescription(), "prototype ship");
  }

  @Test
  @SubjectAware(username = "crato")
  public void testModifyWithoutRequiredPermissions() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    manager.create(heartOfGold);
    heartOfGold.setDescription("prototype ship");

    thrown.expect(UnauthorizedException.class);
    manager.modify(heartOfGold);
  }

  @Test(expected = NotFoundException.class)
  public void testModifyNotFound(){
    manager.modify(createRepositoryWithId());
  }

  @Test
  public void testRefresh() {
    Repository heartOfGold = createTestRepository();
    String description = heartOfGold.getDescription();

    heartOfGold.setDescription("prototype ship");
    manager.refresh(heartOfGold);
    assertEquals(description, heartOfGold.getDescription());
  }

  @Test
  @SubjectAware(username = "crato")
  public void testRefreshWithoutRequiredPermissions() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    manager.create(heartOfGold);
    heartOfGold.setDescription("prototype ship");

    thrown.expect(UnauthorizedException.class);
    manager.refresh(heartOfGold);
  }

  @Test(expected = NotFoundException.class)
  public void testRefreshNotFound(){
    manager.refresh(createRepositoryWithId());
  }

  @Test
  public void testRepositoryHook() {
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
  public void testNamespaceSet() {
    RepositoryManager repoManager = createRepositoryManager(false);
    Repository repository = spy(createTestRepository());
    repository.setName("Testrepo");
    repoManager.create(repository);
    assertEquals("default_namespace", repository.getNamespace());
  }

  @Test
  public void shouldSetNamespace() {
    Repository repository = new Repository(null, "hg", null, "scm");
    manager.create(repository);
    assertNotNull(repository.getId());
    assertNotNull(repository.getNamespace());
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
    DefaultFileSystem fileSystem = new DefaultFileSystem();
    Set<RepositoryHandler> handlerSet = new HashSet<>();
    InitialRepositoryLocationResolver initialRepositoryLocationResolver = new InitialRepositoryLocationResolver();
    XmlRepositoryDAO repositoryDAO = new XmlRepositoryDAO(contextProvider, initialRepositoryLocationResolver, fileSystem);
    RepositoryLocationResolver repositoryLocationResolver = new RepositoryLocationResolver(contextProvider, repositoryDAO, initialRepositoryLocationResolver);
    ConfigurationStoreFactory factory = new JAXBConfigurationStoreFactory(contextProvider, repositoryLocationResolver);
    handlerSet.add(new DummyRepositoryHandler(factory, repositoryLocationResolver));
    handlerSet.add(new DummyRepositoryHandler(factory, repositoryLocationResolver) {
      @Override
      public RepositoryType getType() {
        return new RepositoryType("hg", "Mercurial", Sets.newHashSet());
      }
    });
    handlerSet.add(new DummyRepositoryHandler(factory, repositoryLocationResolver) {
      @Override
      public RepositoryType getType() {
        return new RepositoryType("git", "Git", Sets.newHashSet());
      }
    });


    this.configuration = new ScmConfiguration();

    configuration.setEnableRepositoryArchive(archiveEnabled);

    NamespaceStrategy namespaceStrategy = mock(NamespaceStrategy.class);
    when(namespaceStrategy.createNamespace(Mockito.any(Repository.class))).thenAnswer(invocation -> mockedNamespace);

    return new DefaultRepositoryManager(configuration, contextProvider,
      keyGenerator, repositoryDAO, handlerSet, Providers.of(namespaceStrategy));
  }

  private void createRepository(RepositoryManager m, Repository repository) {
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

  private Repository createRepository(Repository repository) {
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

  private Repository createSecondTestRepository() {
    return createRepository(
      RepositoryTestData.createHappyVerticalPeopleTransporter());
  }

  private Repository createTestRepository() {
    return createRepository(RepositoryTestData.createHeartOfGold());
  }

  private void delete(Manager<Repository> manager, Repository repository){

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
