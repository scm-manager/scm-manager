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



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.group.GroupManagerDecoratorFactory;
import sonia.scm.repository.RepositoryManagerDecoratorFactory;
import sonia.scm.user.UserManagerDecoratorFactory;

/**
 *
 * @author Sebastian Sdorra
 */
public class DecoratorBinder
{

  /**
   * the logger for DecoratorBinder
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DecoratorBinder.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param binder
   */
  public DecoratorBinder(Binder binder)
  {
    userManagerDecoratorFactories = Multibinder.newSetBinder(binder,
      UserManagerDecoratorFactory.class);
    groupManagerDecoratorFactories = Multibinder.newSetBinder(binder,
      GroupManagerDecoratorFactory.class);
    repositoryManagerDecoratorFactories = Multibinder.newSetBinder(binder,
      RepositoryManagerDecoratorFactory.class);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param factoryClass
   */
  public void bindFactory(Class factoryClass)
  {
    if (UserManagerDecoratorFactory.class.isAssignableFrom(factoryClass))
    {
      if (logger.isInfoEnabled())
      {
        logger.info("bind user manager decorator {}");
      }

      userManagerDecoratorFactories.addBinding().to(factoryClass);
    }
    else if (GroupManagerDecoratorFactory.class.isAssignableFrom(factoryClass))
    {
      if (logger.isInfoEnabled())
      {
        logger.info("bind group manager decorator {}");
      }

      groupManagerDecoratorFactories.addBinding().to(factoryClass);
    }
    else if (
      RepositoryManagerDecoratorFactory.class.isAssignableFrom(factoryClass))
    {
      if (logger.isInfoEnabled())
      {
        logger.info("bind repository manager decorator {}");
      }

      repositoryManagerDecoratorFactories.addBinding().to(factoryClass);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Multibinder<GroupManagerDecoratorFactory> groupManagerDecoratorFactories;

  /** Field description */
  private Multibinder<RepositoryManagerDecoratorFactory> repositoryManagerDecoratorFactories;

  /** Field description */
  private Multibinder<UserManagerDecoratorFactory> userManagerDecoratorFactories;
}
