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
