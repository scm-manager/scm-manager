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
    
package sonia.scm.search;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.TransformFilter;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Sebastian Sdorra
 */
public final class SearchUtil
{

  /**
   * the logger for SearchUtil
   */
  private static final Logger logger =
    LoggerFactory.getLogger(SearchUtil.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private SearchUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param value
   * @param other
   *
   * @return
   */
  public static boolean matchesAll(SearchRequest request, String value,
    String... other)
  {
    boolean result = false;
    String query = createStringQuery(request);

    if (value.matches(query))
    {
      result = true;

      for (String o : other)
      {
        if ((o == null) ||!o.matches(query))
        {
          result = false;

          break;
        }
      }
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param value
   * @param other
   *
   * @return
   */
  public static boolean matchesOne(SearchRequest request, String value,
    String... other)
  {
    boolean result = false;
    String query = createStringQuery(request);

    if (!value.matches(query))
    {
      for (String o : other)
      {
        if ((o != null) && o.matches(query))
        {
          result = true;

          break;
        }
      }
    }
    else
    {
      result = true;
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param searchRequest
   * @param collection
   * @param filter
   * @param <T>
   *
   * @return
   */
  public static <T, R> Collection<R> search(SearchRequest searchRequest,
    Collection<T> collection, TransformFilter<T, R> filter)
  {
    List<R> items = new ArrayList<>();
    int index = 0;
    int counter = 0;
    Iterator<T> it = collection.iterator();

    while (it.hasNext())
    {
      R item = filter.accept(it.next());

      if (item != null)
      {
        index++;

        if (searchRequest.getStartWith() <= index)
        {
          items.add(item);
          counter++;

          if (searchRequest.getMaxResults() <= counter)
          {
            break;
          }
        }
      }
    }

    return items;
  }

  /**
   * Method description
   *
   *
   * @param pattern
   * @param c
   */
  private static void appendChar(StringBuilder pattern, char c)
  {
    switch (c)
    {
      case '*' :
        pattern.append(".*");

        break;

      case '?' :
        pattern.append(".");

        break;

      case '(' :
        pattern.append("\\(");

        break;

      case ')' :
        pattern.append("\\)");

        break;

      case '{' :
        pattern.append("\\{");

        break;

      case '}' :
        pattern.append("\\}");

        break;

      case '[' :
        pattern.append("\\[");

        break;

      case ']' :
        pattern.append("\\]");

        break;

      case '|' :
        pattern.append("\\|");

        break;

      case '.' :
        pattern.append("\\.");

        break;

      case '\\' :
        pattern.append("\\\\");

        break;

      default :
        pattern.append(c);
    }
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  private static String createStringQuery(SearchRequest request)
  {
    String query = request.getQuery().trim();

    StringBuilder pattern = new StringBuilder();

    if (request.isIgnoreCase())
    {
      pattern.append("(?i)");
      query = query.toLowerCase(Locale.ENGLISH);
    }

    pattern.append(".*");

    for (char c : query.toCharArray())
    {
      appendChar(pattern, c);
    }

    pattern.append(".*");

    logger.trace("converted query \"{}\" to regex pattern \"{}\"",
      request.getQuery(), pattern);

    return pattern.toString();
  }
}
