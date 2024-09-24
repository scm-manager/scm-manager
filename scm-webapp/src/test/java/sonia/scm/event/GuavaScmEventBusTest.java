/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.event;


import com.github.legman.Subscribe;

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.Subject;

import org.junit.Before;
import org.junit.Test;

import sonia.scm.AbstractTestBase;
import sonia.scm.HandlerEventType;
import sonia.scm.user.User;
import sonia.scm.user.UserEvent;

import static org.junit.Assert.*;

import java.io.IOException;


public class GuavaScmEventBusTest extends AbstractTestBase
{

   @Before
  public void createSubject()
  {
    Subject.Builder builder = new Subject.Builder(new DefaultSecurityManager());

    builder.authenticated(true);

    Subject subject = builder.buildSubject();

    setSubject(subject);
  }


  @Test
  public void testAsyncPost() throws InterruptedException
  {
    thread = null;

    String currentThread = Thread.currentThread().getName();

    LegmanScmEventBus eventBus = new LegmanScmEventBus();

    eventBus.register(new Object()
    {
      @Subscribe
      public void handleEvent(UserEvent event)
      {
        thread = Thread.currentThread().getName();
      }
    });

    eventBus.post(new UserEvent(HandlerEventType.CREATE, new User("test")));

    assertNotEquals(thread, currentThread);
  }

   @Test
  public void testSyncPost()
  {
    thread = null;

    String currentThread = Thread.currentThread().getName();

    LegmanScmEventBus eventBus = new LegmanScmEventBus();

    eventBus.register(new Object()
    {
      @Subscribe(async = false)
      public void handleEvent(Object event)
      {
        thread = Thread.currentThread().getName();
      }
    });

    eventBus.post(new Object());

    assertEquals(thread, currentThread);
  }

  /**
   * TODO replace RuntimeException with EventBusException in legman 1.2.0.
   *
   */
  @Test(expected = RuntimeException.class)
  public void testSyncPostWithCheckedException()
  {
    LegmanScmEventBus eventBus = new LegmanScmEventBus();

    eventBus.register(new Object()
    {

      @Subscribe(async = false)
      public void handleEvent(Object event) throws IOException
      {
        throw new IOException("could not handle event");
      }

    });

    eventBus.post(new Object());
  }

   @Test(expected = RuntimeException.class)
  public void testSyncPostWithRuntimeException()
  {
    LegmanScmEventBus eventBus = new LegmanScmEventBus();

    eventBus.register(new Object()
    {

      @Subscribe(async = false)
      public void handleEvent(Object event)
      {
        throw new RuntimeException("could not handle event");
      }

    });

    eventBus.post(new Object());
  }

  //~--- fields ---------------------------------------------------------------

  private String thread;
}
