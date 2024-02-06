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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.HandlerEventType;


public final class UserEventHack
{

 
  private static final Logger logger =
    LoggerFactory.getLogger(UserEventHack.class);


  private UserEventHack() {}


 
  public static void fireEvent(UserManager userManager, HandlerEventType event,
    User user)
  {
    AbstractUserManager abstractUserManager =
      getAbstractUserManager(userManager);

    if (abstractUserManager != null)
    {
      abstractUserManager.fireEvent(event, user);
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("user manager is not an instance of AbstractUserManager");
    }
  }



  private static AbstractUserManager getAbstractUserManager(
    UserManager userManager)
  {
    AbstractUserManager abstractUserManager = null;

    if (userManager instanceof UserManagerDecorator)
    {
      abstractUserManager = getAbstractUserManager(
        ((UserManagerDecorator) userManager).getDecorated());
    }
    else if (userManager instanceof AbstractUserManager)
    {
      abstractUserManager = (AbstractUserManager) userManager;
    }

    return abstractUserManager;
  }
}
