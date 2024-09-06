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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;


public final class ServiceUtil
{

  private ServiceUtil() {}

  public static <T> T getService(Class<T> type, T def)
  {
    T result = getService(type);

    if (result == null)
    {
      result = def;
    }

    return result;
  }

  public static <T> T getService(Class<T> type)
  {
    T result = null;

    try
    {
      ServiceLoader<T> loader = ServiceLoader.load(type);

      if (loader != null)
      {
        result = loader.iterator().next();
      }
    }
    catch (NoSuchElementException ex)
    {

      // do nothing
    }

    return result;
  }

  public static <T> List<T> getServices(Class<T> type)
  {
    List<T> result = new ArrayList<>();

    try
    {
      ServiceLoader<T> loader = ServiceLoader.load(type);

      if (loader != null)
      {
        for (T service : loader)
        {
          result.add(service);
        }
      }
    }
    catch (NoSuchElementException ex)
    {

      // do nothing
    }

    return result;
  }
}
