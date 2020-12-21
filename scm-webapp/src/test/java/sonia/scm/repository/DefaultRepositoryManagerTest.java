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

package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.github.legman.Subscribe;
import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.ImmutableSet;
import com.google.inject.util.Providers;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.util.ThreadContext;
import org.assertj.core.api.Assertions;
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
import sonia.scm.NoChangesMadeException;
import sonia.scm.NotFoundException;
import sonia.scm.SCMContext;
import sonia.scm.ScmConstraintViolationException;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.spi.HookContextProvider;
import sonia.scm.security.DefaultKeyGenerator;
import sonia.scm.security.KeyGenerator;
import sonia.scm.store.ConfigurationStoreFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import sonia.scm.TempSCMContextProvider;

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

  private RepositoryDAO repositoryDAO;

  {
    ThreadContext.unbindSubject();
  }

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private NamespaceStrategy namespaceStrategy = mock(NamespaceStrategy.class);

  private ScmConfiguration configuration;

  private String mockedNamespace = "default_namespace";

  @Before
  public void initContext() {
    ((TempSCMContextProvider) SCMContext.getContext()).setBaseDirectory(temp);
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
    createTestRepository();
    thrown.expect(AlreadyExistsException.class);
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

  @Test(expected = NotFoundException.class)
  public void testDeleteNotFound() {
    manager.delete(createRepositoryWithId());
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
      } else if (repository.getId().equals(happyVerticalPeopleTransporter.getId())) {
        assertRepositoriesEquals(happyVerticalPeopleTransporter, repository);
        foundTransporter = true;
      }
    }

    assertTrue(foundHeart);
    assertTrue(foundTransporter);

    // test for reference
    assertNotSame(heartOfGold, heartReference);
    heartReference.setDescription("prototype ship");
    assertNotEquals(heartOfGold.getDescription(), heartReference.getDescription());
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
    RepositoryManager repositoryManager = createRepositoryManager(keyGenerator);

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
    RepositoryManager repoManager = createManager();
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
    assertEquals("prototype ship", hearReference.getDescription());
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
  public void testModifyNotFound() {
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
  public void testRefreshNotFound() {
    manager.refresh(createRepositoryWithId());
  }

  @Test
  public void testRepositoryHook() {
    CountingReceiveHook hook = new CountingReceiveHook();
    RepositoryManager repoManager = createManager();

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
    RepositoryManager repoManager = createManager();
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

  @Test
  public void shouldThrowChangeNamespaceNotAllowedException() {
    Repository repository = new Repository("1", "hg", "space", "x");
    RepositoryManager repoManager = createManager();
    when(namespaceStrategy.canBeChanged()).thenReturn(false);

    thrown.expect(ChangeNamespaceNotAllowedException.class);

    repoManager.rename(repository, "hitchhiker", "heart-of-gold");
  }

  @Test
  public void shouldThrowNoChangesMadeException() {
    Repository repository = createTestRepository();
    RepositoryManager repoManager = createManager();

    thrown.expect(NoChangesMadeException.class);

    repoManager.rename(repository, "default_namespace", "HeartOfGold");
  }

  @Test
  public void shouldThrowValidationException() {
    Repository repository = createTestRepository();
    RepositoryManager repoManager = createManager();
    when(namespaceStrategy.canBeChanged()).thenReturn(true);
    when(namespaceStrategy.createNamespace(argThat(r -> r.getNamespace().equals("invalid")))).thenThrow(ScmConstraintViolationException.class);

    thrown.expect(ScmConstraintViolationException.class);

    repoManager.rename(repository, "invalid", "splendid");
  }

  @Test
  public void shouldOnlyChangeRepositoryName() {
    Repository repository = createTestRepository();
    RepositoryManager repoManager = (RepositoryManager) manager;

    Repository changedRepo = repoManager.rename(repository, "default_namespace", "puzzle42");
    assertNotEquals(changedRepo.getName(), repository.getName());
  }

  @Test
  public void shouldRenameRepositoryNamespaceAndName() {
    Repository repository = createTestRepository();
    RepositoryManager repoManager = (RepositoryManager) manager;
    when(namespaceStrategy.canBeChanged()).thenReturn(true);
    when(namespaceStrategy.createNamespace(any(Repository.class))).thenReturn("hitchhiker");

    Repository changedRepo = repoManager.rename(repository, "hitchhiker", "puzzle42");
    assertEquals("puzzle42", changedRepo.getName());
    assertEquals("hitchhiker", changedRepo.getNamespace());
  }

  @Test
  public void shouldReturnDistinctNamespaces() {
    createTestRepository();
    createSecondTestRepository();

    Collection<String> namespaces = ((RepositoryManager) manager).getAllNamespaces();

    Assertions.assertThat(namespaces)
      .hasSize(1)
      .contains("default_namespace");
  }

  @Test
  public void shouldMarkRepositoryAsArchived() {
    Repository repository = createTestRepository();
    RepositoryManager repoManager = (RepositoryManager) manager;

    repoManager.archive(repository);

    verify(repositoryDAO).modify(argThat(Repository::isArchived));
  }

  @Test
  @SubjectAware(username = "dent")
  public void shouldNotMarkRepositoryAsArchivedWithoutPermission() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    when(repositoryDAO.get(repository.getNamespaceAndName())).thenReturn(repository);
    when(repositoryDAO.get(repository.getId())).thenReturn(repository);
    RepositoryManager repoManager = (RepositoryManager) manager;

    assertThrows(UnauthorizedException.class, () -> repoManager.archive(repository));

    verify(repositoryDAO, never()).modify(any());
  }

  @Test
  public void shouldNotMarkRepositoryAsArchivedTwice() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    repository.setArchived(true);
    createRepository(repository);
    RepositoryManager repoManager = (RepositoryManager) manager;

    assertThrows(NoChangesMadeException.class, () -> repoManager.archive(repository));

    verify(repositoryDAO, never()).modify(any());
  }

  @Test
  public void shouldRemoveArchiveMarkFromRepository() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    repository.setArchived(true);
    createRepository(repository);
    RepositoryManager repoManager = (RepositoryManager) manager;

    repoManager.unarchive(repository);

    verify(repositoryDAO).modify(argThat(r -> !r.isArchived()));
  }

  @Test
  @SubjectAware(username = "dent")
  public void shouldNotRemoveArchiveMarkFromRepositoryWithoutPermission() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    when(repositoryDAO.get(repository.getNamespaceAndName())).thenReturn(repository);
    when(repositoryDAO.get(repository.getId())).thenReturn(repository);
    repository.setArchived(true);
    RepositoryManager repoManager = (RepositoryManager) manager;

    assertThrows(UnauthorizedException.class, () -> repoManager.unarchive(repository));

    verify(repositoryDAO, never()).modify(any());
  }

  @Test
  public void shouldNotRemoveArchiveMarkFromNotArchivedRepository() {
    Repository repository = createTestRepository();
    RepositoryManager repoManager = (RepositoryManager) manager;

    assertThrows(NoChangesMadeException.class, () -> repoManager.unarchive(repository));

    verify(repositoryDAO, never()).modify(any());
  }

  //~--- methods --------------------------------------------------------------

  @Override
  protected DefaultRepositoryManager createManager() {
    return createRepositoryManager(new DefaultKeyGenerator());
  }

  private DefaultRepositoryManager createRepositoryManager(KeyGenerator keyGenerator) {
    Set<RepositoryHandler> handlerSet = new HashSet<>();
    repositoryDAO = createRepositoryDaoMock();
    mock(ConfigurationStoreFactory.class);
    handlerSet.add(createRepositoryHandler("dummy", "Dummy"));
    handlerSet.add(createRepositoryHandler("git", "Git"));
    handlerSet.add(createRepositoryHandler("hg", "Mercurial"));
    handlerSet.add(createRepositoryHandler("svn", "SVN"));

    this.configuration = new ScmConfiguration();

    when(namespaceStrategy.createNamespace(Mockito.any(Repository.class))).thenAnswer(invocation -> mockedNamespace);

    return new DefaultRepositoryManager(configuration, contextProvider,
      keyGenerator, repositoryDAO, handlerSet, Providers.of(namespaceStrategy));
  }

  private RepositoryDAO createRepositoryDaoMock() {
    Map<String, Repository> repositoriesById = new HashMap<>();
    Map<NamespaceAndName, Repository> repositoriesByNamespaceAndName = new HashMap<>();
    RepositoryDAO mock = mock(RepositoryDAO.class);
    doAnswer(invocation -> {
      Repository repo = invocation.getArgument(0, Repository.class);
      if (repositoriesById.containsKey(repo.getId()) || repositoriesByNamespaceAndName.containsKey(repo.getNamespaceAndName())) {
        throw new AlreadyExistsException(repo);
      }
      Repository clone = repo.clone();
      repositoriesById.put(repo.getId(), clone);
      repositoriesByNamespaceAndName.put(repo.getNamespaceAndName(), clone);
      return null;
    }).when(mock).add(any());
    doAnswer(invocation -> {
      Repository repo = invocation.getArgument(0, Repository.class);
      Repository clone = repo.clone();
      repositoriesById.put(repo.getId(), clone);
      repositoriesByNamespaceAndName.put(repo.getNamespaceAndName(), clone);
      return null;
    }).when(mock).modify(any());
    when(mock.get(anyString())).thenAnswer(invocation -> repositoriesById.get(invocation.getArgument(0, String.class)));
    when(mock.get(any(NamespaceAndName.class))).thenAnswer(invocation -> repositoriesByNamespaceAndName.get(invocation.getArgument(0, NamespaceAndName.class)));
    when(mock.getAll()).thenAnswer(invocation -> repositoriesById.values());
    when(mock.contains(anyString())).thenAnswer(invocation -> repositoriesById.containsKey(invocation.getArgument(0, String.class)));
    when(mock.contains(any(Repository.class))).thenAnswer(invocation -> repositoriesById.containsKey(invocation.getArgument(0, Repository.class).getId()));
    doAnswer(invocation -> {
      Repository repo = invocation.getArgument(0, Repository.class);
      repositoriesById.remove(repo.getId());
      repositoriesByNamespaceAndName.remove(repo.getNamespaceAndName());
      return null;
    }).when(mock).delete(any(Repository.class));
    return mock;
  }

  private RepositoryHandler createRepositoryHandler(String name, String diplayName) {
    RepositoryHandler handler = mock(RepositoryHandler.class);
    when(handler.getType()).thenReturn(new RepositoryType(name, diplayName, emptySet()));
    when(handler.isConfigured()).thenReturn(true);
    return handler;
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

  private void delete(Manager<Repository> manager, Repository repository) {

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
      } else if (event.getEventType().isPre()) {
        this.preRepository = event.getItem();
        this.preEvent = event.getEventType();
      }
    }
  }

}
