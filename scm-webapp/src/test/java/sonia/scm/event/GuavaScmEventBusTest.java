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



package sonia.scm.event;

//~--- non-JDK imports --------------------------------------------------------

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

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class GuavaScmEventBusTest extends AbstractTestBase
{

  /**
   * Method description
   *
   */
  @Before
  public void createSubject()
  {
    Subject.Builder builder = new Subject.Builder(new DefaultSecurityManager());

    builder.authenticated(true);

    Subject subject = builder.buildSubject();

    setSubject(subject);
  }

  /**
   * Method description
   *
   *
   * @throws InterruptedException
   */
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

  /**
   * Method description
   *
   */
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

  /**
   * Method description
   *
   */
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

  /** Field description */
  private String thread;
}
