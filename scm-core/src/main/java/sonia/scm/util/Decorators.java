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

package sonia.scm.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.DecoratorFactory;


public final class Decorators
{

  private static final Logger logger =
    LoggerFactory.getLogger(Decorators.class);


  private Decorators() {}

  public static <T> T decorate(T object,
    Iterable<? extends DecoratorFactory<T>> decoratorFactories)
  {
    if (decoratorFactories != null)
    {
      for (DecoratorFactory<T> decoratorFactory : decoratorFactories)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("decorate {} with {}", object.getClass(),
            decoratorFactory.getClass());
        }

        object = decoratorFactory.createDecorator(object);
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("no decorators found for {}", object.getClass());
    }

    return object;
  }
}
