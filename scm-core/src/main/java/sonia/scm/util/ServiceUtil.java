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
