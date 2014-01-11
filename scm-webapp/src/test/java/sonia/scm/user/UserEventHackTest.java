/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.user;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import sonia.scm.HandlerEventType;

import static org.mockito.Mockito.*;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class UserEventHackTest
{

  /**
   * Method description
   *
   */
  @Before
  public void before()
  {
    verifier = inOrder(userManager);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testFireEvent()
  {
    fireEvent(userManager);

    verify(1);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testFireEventDecorated()
  {

    UserManager u = new UserManagerDecorator(userManager);

    fireEvent(u);

    verify(1);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testFireEventMultiDecorated()
  {

    UserManager u = new UserManagerDecorator(userManager);

    u = new UserManagerDecorator(u);
    u = new UserManagerDecorator(u);
    u = new UserManagerDecorator(u);
    u = new UserManagerDecorator(u);

    fireEvent(u);

    verify(1);
  }

  /**
   * Method description
   *
   *
   * @param u
   */
  private void fireEvent(UserManager u)
  {
    UserEventHack.fireEvent(u, HandlerEventType.CREATE, new User());
  }

  /**
   * Method description
   *
   *
   * @param count
   */
  private void verify(int count)
  {
    verifier.verify(userManager,
      calls(count)).fireEvent(any(HandlerEventType.class), any(User.class));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Mock
  private AbstractUserManager userManager;

  /** Field description */
  private InOrder verifier;
}
