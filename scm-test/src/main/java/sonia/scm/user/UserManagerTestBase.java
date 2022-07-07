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

package sonia.scm.user;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.junit.Test;
import sonia.scm.AlreadyExistsException;
import sonia.scm.Manager;
import sonia.scm.ManagerTestBase;
import sonia.scm.NotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

//~--- JDK imports ------------------------------------------------------------

public abstract class UserManagerTestBase extends ManagerTestBase<User> {

  public static final int THREAD_COUNT = 10;

  @Test
  public void testCreate() {
    User zaphod = UserTestData.createZaphod();

    manager.create(zaphod);

    User otherUser = manager.get("zaphod");

    assertNotNull(otherUser);
    assertUserEquals(zaphod, otherUser);
  }

  @Test(expected = AlreadyExistsException.class)
  public void testCreateExisting() {
    User zaphod = UserTestData.createZaphod();

    manager.create(zaphod);
    assertNotNull(manager.get("zaphod"));

    User sameUser = UserTestData.createZaphod();

    manager.create(sameUser);
  }

  @Test
  public void testDelete() {
    User zaphod = UserTestData.createZaphod();

    manager.create(zaphod);
    assertNotNull(manager.get("zaphod"));
    manager.delete(zaphod);
    assertNull(manager.get("zaphod"));
  }

  @Test(expected = NotFoundException.class)
  public void testDeleteNotFound() {
    manager.delete(UserTestData.createDent());
  }

  @Test
  public void testGet() {
    User zaphod = UserTestData.createZaphod();

    manager.create(zaphod);
    assertNotNull(manager.get("zaphod"));

    // test for reference
    zaphod.setDisplayName("Tricia McMillan");
    zaphod = manager.get("zaphod");
    assertNotNull(zaphod);
    assertEquals("Zaphod Beeblebrox", zaphod.getDisplayName());
  }

  @Test
  public void testGetAll() {
    User zaphod = UserTestData.createZaphod();

    manager.create(zaphod);
    assertNotNull(manager.get("zaphod"));

    User trillian = UserTestData.createTrillian();

    manager.create(trillian);
    assertNotNull(manager.get("trillian"));

    boolean foundZaphod = false;
    boolean foundTrillian = false;
    Collection<User> users = manager.getAll();

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

    for (User u : manager.getAll())
    {
      if (u.getName().equals("trillian"))
      {
        reference = u;
      }
    }

    assertNotNull(reference);
    assertEquals(reference.getDisplayName(), "Tricia McMillan");
  }

  @Test
  public void testModify() {
    User zaphod = UserTestData.createZaphod();

    manager.create(zaphod);
    assertNotNull(manager.get("zaphod"));
    zaphod.setDisplayName("Tricia McMillan");
    manager.modify(zaphod);

    User otherUser = manager.get("zaphod");

    assertNotNull(otherUser);
    assertEquals(otherUser.getDisplayName(), "Tricia McMillan");
  }

  @Test(expected = NotFoundException.class)
  public void testModifyNotExisting() {
    manager.modify(UserTestData.createZaphod());
  }

  @Test
  public void testMultiThreaded() throws InterruptedException {
    int initialSize = manager.getAll().size();
    List<MultiThreadTester> testers = new ArrayList<MultiThreadTester>();

    for (int i = 0; i < THREAD_COUNT; i++)
    {
      testers.add(new MultiThreadTester(manager));
    }

    Subject subject = SecurityUtils.getSubject();

    for (MultiThreadTester tester : testers)
    {
      subject.associateWith(tester);
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

    assertTrue((initialSize + THREAD_COUNT) == manager.getAll().size());
  }

  @Test
  public void testRefresh() {
    User zaphod = UserTestData.createZaphod();

    manager.create(zaphod);
    assertNotNull(manager.get("zaphod"));
    zaphod.setDisplayName("Tricia McMillan");
    manager.refresh(zaphod);
    assertEquals(zaphod.getDisplayName(), "Zaphod Beeblebrox");
  }

  @Test(expected = NotFoundException.class)
  public void testRefreshNotFound(){
    manager.refresh(UserTestData.createDent());
  }

  private void assertUserEquals(User user, User otherUser)
  {
    assertEquals(user.getName(), otherUser.getName());
    assertEquals(user.getDisplayName(), otherUser.getDisplayName());
    assertEquals(user.getMail(), otherUser.getMail());
    assertEquals(user.getPassword(), otherUser.getPassword());
  }

  private static class MultiThreadTester implements Runnable
  {

    public MultiThreadTester(Manager<User> userManager)
    {
      this.manager = userManager;
    }

    //~--- methods ------------------------------------------------------------

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

    private User createUser() {
      String id = UUID.randomUUID().toString();
      User user = new User(id, id.concat(" displayName"),
                    id.concat("@mail.com"));
      user.setPassword("$shiro1$secret");

      manager.create(user);

      return user;
    }

    private void modifyAndDeleteUser(User user) {
      String name = user.getName();
      String nd = name.concat(" new displayname");

      user.setDisplayName(nd);
      manager.modify(user);

      User otherUser = manager.get(name);

      assertNotNull(otherUser);
      assertEquals(nd, otherUser.getDisplayName());
      manager.delete(user);
      otherUser = manager.get(name);
      assertNull(otherUser);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private boolean finished = false;

    /** Field description */
    private Manager<User> manager;
  }
}
