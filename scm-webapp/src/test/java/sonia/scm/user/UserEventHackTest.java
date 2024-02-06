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


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import sonia.scm.HandlerEventType;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class UserEventHackTest
{

   @Before
  public void before()
  {
    verifier = inOrder(userManager);
  }

   @Test
  public void testFireEvent()
  {
    fireEvent(userManager);

    verify(1);
  }

   @Test
  public void testFireEventDecorated()
  {

    UserManager u = new UserManagerDecorator(userManager);

    fireEvent(u);

    verify(1);
  }

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


  private void fireEvent(UserManager u)
  {
    UserEventHack.fireEvent(u, HandlerEventType.CREATE, new User());
  }


  private void verify(int count)
  {
    verifier.verify(userManager,
      calls(count)).fireEvent(any(HandlerEventType.class), any(User.class));
  }

  //~--- fields ---------------------------------------------------------------

  @Mock
  private AbstractUserManager userManager;

  private InOrder verifier;
}
