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



package sonia.scm.user;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sonia.scm.SCMContextProvider;
import sonia.scm.util.IOUtil;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class UserManagerTestBase
{

  /** Field description */
  public static final int THREAD_COUNT = 10;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public abstract UserManager createUserHandler();

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @After
  public void tearDownTest() throws IOException
  {
    try
    {
      userManager.close();
    }
    finally
    {
      IOUtil.delete(tempDirectory);
    }
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws UserException
   */
  @Test
  public void testCreate() throws UserException, IOException
  {
    User zaphod = getTestUser();

    userManager.create(zaphod);

    User otherUser = userManager.get("zaphod");

    assertNotNull(otherUser);
    assertUserEquals(zaphod, otherUser);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws UserException
   */
  @Test(expected = UserAllreadyExistException.class)
  public void testCreateExisting() throws UserException, IOException
  {
    User zaphod = getTestUser();

    userManager.create(zaphod);
    assertNotNull(userManager.get("zaphod"));

    User sameUser = getTestUser();

    userManager.create(sameUser);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws UserException
   */
  @Test
  public void testDelete() throws UserException, IOException
  {
    User zaphod = getTestUser();

    userManager.create(zaphod);
    assertNotNull(userManager.get("zaphod"));
    userManager.delete(zaphod);
    assertNull(userManager.get("zaphod"));
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws UserException
   */
  @Test
  public void testGet() throws UserException, IOException
  {
    User zaphod = getTestUser();

    userManager.create(zaphod);
    assertNotNull(userManager.get("zaphod"));

    // test for reference
    zaphod.setDisplayName("Tricia McMillan");
    zaphod = userManager.get("zaphod");
    assertNotNull(zaphod);
    assertEquals("Zaphod Beeblebrox", zaphod.getDisplayName());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws UserException
   */
  @Test
  public void testGetAll() throws UserException, IOException
  {
    User zaphod = getTestUser();

    userManager.create(zaphod);
    assertNotNull(userManager.get("zaphod"));

    User trillian = new User("trillian", "Tricia McMillan",
                             "tricia.mcmillan@hitchhiker.com");

    userManager.create(trillian);
    assertNotNull(userManager.get("trillian"));

    boolean foundZaphod = false;
    boolean foundTrillian = false;
    Collection<User> users = userManager.getAll();

    assertNotNull(users);
    assertFalse(users.isEmpty());
    assertTrue(users.size() >= 2);

    for (User u : users)
    {
      if (u.getName().equals("zaphod"))
      {
        foundZaphod = true;
        assertUserEquals(zaphod, u);
      }
      else if (u.getName().equals("trillian"))
      {
        foundTrillian = true;
        assertUserEquals(trillian, u);
      }
    }

    assertTrue(foundZaphod);
    assertTrue(foundTrillian);

    // test for reference
    trillian = null;

    for (User u : users)
    {
      if (u.getName().equals("trillian"))
      {
        trillian = u;
      }
    }

    assertNotNull(trillian);
    trillian.setDisplayName("Zaphod Beeblebrox");

    User reference = null;

    for (User u : userManager.getAll())
    {
      if (u.getName().equals("trillian"))
      {
        reference = u;
      }
    }

    assertNotNull(reference);
    assertEquals(reference.getDisplayName(), "Tricia McMillan");
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws UserException
   */
  @Test
  public void testModify() throws UserException, IOException
  {
    User zaphod = getTestUser();

    userManager.create(zaphod);
    assertNotNull(userManager.get("zaphod"));
    zaphod.setDisplayName("Tricia McMillan");
    userManager.modify(zaphod);

    User otherUser = userManager.get("zaphod");

    assertNotNull(otherUser);
    assertEquals(otherUser.getDisplayName(), "Tricia McMillan");
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws UserException
   */
  @Test(expected = UserException.class)
  public void testModifyNotExisting() throws UserException, IOException
  {
    User zaphod = getTestUser();

    userManager.modify(zaphod);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws InterruptedException
   * @throws UserException
   */
  @Test
  public void testMultiThreaded()
          throws UserException, IOException, InterruptedException
  {
    int initialSize = userManager.getAll().size();
    List<MultiThreadTester> testers = new ArrayList<MultiThreadTester>();

    for (int i = 0; i < THREAD_COUNT; i++)
    {
      testers.add(new MultiThreadTester(userManager));
    }

    for (MultiThreadTester tester : testers)
    {
      new Thread(tester).start();
    }

    boolean fin = false;

    while (!fin)
    {
      Thread.sleep(100l);
      fin = true;

      for (MultiThreadTester tester : testers)
      {
        if (!tester.finished)
        {
          fin = false;
        }
      }
    }

    assertTrue((initialSize + THREAD_COUNT) == userManager.getAll().size());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws UserException
   */
  @Test
  public void testRefresh() throws UserException, IOException
  {
    User zaphod = getTestUser();

    userManager.create(zaphod);
    assertNotNull(userManager.get("zaphod"));
    zaphod.setDisplayName("Tricia McMillan");
    userManager.refresh(zaphod);
    assertEquals(zaphod.getDisplayName(), "Zaphod Beeblebrox");
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Before
  public void setUpTest()
  {
    tempDirectory = new File(System.getProperty("java.io.tmpdir"),
                             UUID.randomUUID().toString());
    assertTrue(tempDirectory.mkdirs());
    userManager = createUserHandler();

    SCMContextProvider provider = mock(SCMContextProvider.class);

    when(provider.getBaseDirectory()).thenReturn(tempDirectory);
    userManager.init(provider);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param user
   * @param otherUser
   */
  private void assertUserEquals(User user, User otherUser)
  {
    assertEquals(user.getName(), otherUser.getName());
    assertEquals(user.getDisplayName(), otherUser.getDisplayName());
    assertEquals(user.getMail(), otherUser.getMail());
    assertEquals(user.getPassword(), otherUser.getPassword());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private User getTestUser()
  {
    return new User("zaphod", "Zaphod Beeblebrox",
                    "zaphod.beeblebrox@hitchhiker.com");
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 2010-11-23
   * @author         Sebastian Sdorra
   */
  private static class MultiThreadTester implements Runnable
  {

    /**
     * Constructs ...
     *
     *
     * @param userManager
     */
    public MultiThreadTester(UserManager userManager)
    {
      this.userManager = userManager;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     */
    @Override
    public void run()
    {
      try
      {
        User user = createUser();

        modifyAndDeleteUser(user);
        createUser();
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);
      }

      finished = true;
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     * @throws UserException
     */
    private User createUser() throws UserException, IOException
    {
      String id = UUID.randomUUID().toString();
      User user = new User(id, id.concat(" displayName"),
                           id.concat("@mail.com"));

      userManager.create(user);

      return user;
    }

    /**
     * Method description
     *
     *
     * @param user
     *
     * @throws IOException
     * @throws UserException
     */
    private void modifyAndDeleteUser(User user)
            throws UserException, IOException
    {
      String name = user.getName();
      String nd = name.concat(" new displayname");

      user.setDisplayName(nd);
      userManager.modify(user);

      User otherUser = userManager.get(name);

      assertNotNull(otherUser);
      assertEquals(nd, otherUser.getDisplayName());
      userManager.delete(user);
      otherUser = userManager.get(name);
      assertNull(otherUser);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private boolean finished = false;

    /** Field description */
    private UserManager userManager;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File tempDirectory;

  /** Field description */
  private UserManager userManager;
}
