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
