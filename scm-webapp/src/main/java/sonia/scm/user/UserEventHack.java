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
