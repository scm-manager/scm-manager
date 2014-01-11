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

import org.apache.shiro.subject.Subject;

import org.junit.Before;
import org.junit.Test;

import sonia.scm.HandlerEventType;
import sonia.scm.Manager;
import sonia.scm.ManagerTestBase;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.api.HookContext;
import sonia.scm.util.MockUtil;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class RepositoryManagerTestBase
  extends ManagerTestBase<Repository, RepositoryException>
{

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  public abstract HookContext createHookContext(Repository repository);

  /**
   * Method description
   *
   *
   * @param archiveEnabled
   *
   * @return
   */
  protected abstract RepositoryManager createRepositoryManager(
    boolean archiveEnabled);

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testCreate() throws RepositoryException, IOException
  {
    Repository heartOfGold = createTestRepository();
    Repository dbRepo = manager.get(heartOfGold.getId());

    assertNotNull(dbRepo);
    assertRepositoriesEquals(dbRepo, heartOfGold);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test(expected = RepositoryAllreadyExistExeption.class)
  public void testCreateExisting() throws RepositoryException, IOException
  {
    createTestRepository();
    createTestRepository();
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testDelete() throws RepositoryException, IOException
  {
    delete(manager, createTestRepository());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test(expected = RepositoryIsNotArchivedException.class)
  public void testDeleteNonArchived() throws RepositoryException, IOException
  {
    delete(createRepositoryManager(true), createTestRepository());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test(expected = RepositoryNotFoundException.class)
  public void testDeleteNotFound() throws RepositoryException, IOException
  {
    manager.delete(createRepositoryWithId());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testDeleteWithEnabledArchive()
    throws RepositoryException, IOException
  {
    Repository repository = createTestRepository();

    repository.setArchived(true);
    delete(createRepositoryManager(true), repository);
  }

  /**
   *  Method description
   *
   *
   *  @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testGet() throws RepositoryException, IOException
  {
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

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testGetAll() throws RepositoryException, IOException
  {
    Repository heartOfGold = createTestRepository();
    Repository happyVerticalPeopleTransporter = createSecondTestRepository();
    boolean foundHeart = false;
    boolean foundTransporter = false;
    Collection<Repository> repositories = manager.getAll();

    assertNotNull(repositories);
    assertFalse(repositories.isEmpty());
    assertTrue(repositories.size() >= 2);

    Repository heartReference = null;

    for (Repository repository : repositories)
    {
      if (repository.getId().equals(heartOfGold.getId()))
      {
        assertRepositoriesEquals(heartOfGold, repository);
        foundHeart = true;
        heartReference = repository;
      }
      else if (
        repository.getId().equals(happyVerticalPeopleTransporter.getId()))
      {
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

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testListener() throws RepositoryException, IOException
  {
    RepositoryManager repoManager = createRepositoryManager(false);
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

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testModify() throws RepositoryException, IOException
  {
    Repository heartOfGold = createTestRepository();

    heartOfGold.setDescription("prototype ship");
    manager.modify(heartOfGold);

    Repository hearReference = manager.get(heartOfGold.getId());

    assertNotNull(hearReference);
    assertEquals(hearReference.getDescription(), "prototype ship");
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test(expected = RepositoryNotFoundException.class)
  public void testModifyNotFound() throws RepositoryException, IOException
  {
    manager.modify(createRepositoryWithId());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testRefresh() throws RepositoryException, IOException
  {
    Repository heartOfGold = createTestRepository();
    String description = heartOfGold.getDescription();

    heartOfGold.setDescription("prototype ship");
    manager.refresh(heartOfGold);
    assertEquals(description, heartOfGold.getDescription());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test(expected = RepositoryNotFoundException.class)
  public void testRefreshNotFound() throws RepositoryException, IOException
  {
    manager.refresh(createRepositoryWithId());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testRepositoryHook() throws RepositoryException, IOException
  {
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

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Before
  public void setAdminSubject()
  {
    Subject admin = MockUtil.createAdminSubject();

    setSubject(admin);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repo
   * @param other
   */
  private void assertRepositoriesEquals(Repository repo, Repository other)
  {
    assertEquals(repo.getId(), other.getId());
    assertEquals(repo.getName(), other.getName());
    assertEquals(repo.getDescription(), other.getDescription());
    assertEquals(repo.getContact(), other.getContact());
    assertEquals(repo.getCreationDate(), other.getCreationDate());
    assertEquals(repo.getLastModified(), other.getLastModified());
  }

  /**
   * Method description
   *
   *
   *
   * @param repository
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private Repository createRepository(Repository repository)
    throws RepositoryException, IOException
  {
    manager.create(repository);
    assertNotNull(repository.getId());
    assertNotNull(manager.get(repository.getId()));
    assertTrue(repository.getCreationDate() > 0);

    return repository;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private Repository createRepositoryWithId()
  {
    Repository repository = RepositoryTestData.createHeartOfGold();

    repository.setId("abc");

    return repository;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private Repository createSecondTestRepository()
    throws RepositoryException, IOException
  {
    return createRepository(
      RepositoryTestData.createHappyVerticalPeopleTransporter());
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private Repository createTestRepository()
    throws RepositoryException, IOException
  {
    return createRepository(RepositoryTestData.createHeartOfGold());
  }

  /**
   * Method description
   *
   *
   * @param manager
   * @param repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private void delete(Manager<Repository, RepositoryException> manager,
    Repository repository)
    throws RepositoryException, IOException
  {
    String id = repository.getId();

    manager.delete(repository);
    assertNull(manager.get(id));
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/01/29
   * @author         Enter your name here...
   */
  private static class CountingReceiveHook
  {

    /**
     * Method description
     *
     *
     * @param event
     */
    @Subscribe(async = false)
    public void onEvent(PostReceiveRepositoryHookEvent event)
    {
      eventsReceived++;
    }

    /**
     * Method description
     *
     *
     * @param event
     */
    @Subscribe(async = false)
    public void onEvent(PreReceiveRepositoryHookEvent event)
    {
      eventsReceived++;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private int eventsReceived = 0;
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/01/29
   * @author         Enter your name here...
   */
  private static class TestListener
  {

    /**
     * Method description
     *
     *
     * @param repository
     * @param event
     */
    @Subscribe(async = false)
    public void onEvent(RepositoryEvent event)
    {
      if (event.getEventType().isPost())
      {
        this.postRepository = event.getItem();
        this.postEvent = event.getEventType();
      }
      else if (event.getEventType().isPre())
      {
        this.preRepository = event.getItem();
        this.preEvent = event.getEventType();
      }
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private HandlerEventType postEvent;

    /** Field description */
    private Repository postRepository;

    /** Field description */
    private HandlerEventType preEvent;

    /** Field description */
    private Repository preRepository;
  }
}
