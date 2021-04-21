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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.TransformFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public final class SearchUtil {

  private static final Logger LOG = LoggerFactory.getLogger(SearchUtil.class);

  private SearchUtil() {
  }

  public static boolean matchesAll(SearchRequest request, String value,
                                   String... other) {
    String query = createStringQuery(request);

    if (value.matches(query)) {
      for (String o : other) {
        if ((o == null) || !o.matches(query)) {
          return false;
        }
      }
      return true;
    }

    return false;
  }

  public static boolean matchesOne(SearchRequest request, String value,
                                   String... other) {
    String query = createStringQuery(request);

    if (!value.matches(query)) {
      for (String o : other) {
        if ((o != null) && o.matches(query)) {
          return true;
        }
      }
    } else {
      return true;
    }

    return false;
  }

  public static <T, R> Collection<R> search(SearchRequest searchRequest,
                                            Collection<T> collection, TransformFilter<T, R> filter) {
    List<R> items = new ArrayList<>();
    int index = 0;
    int counter = 0;

    for (T t : collection) {
      R item = filter.accept(t);

      if (item != null) {
        index++;

        if (searchRequest.getStartWith() <= index) {
          items.add(item);
          counter++;

          if (searchRequest.getMaxResults() > 0 && searchRequest.getMaxResults() <= counter) {
            break;
          }
        }
      }
    }

    return items;
  }

  private static void appendChar(StringBuilder pattern, char c) {
    switch (c) {
      case '*':
        pattern.append(".*");

        break;

      case '?':
        pattern.append(".");

        break;

      case '(':
        pattern.append("\\(");

        break;

      case ')':
        pattern.append("\\)");

        break;

      case '{':
        pattern.append("\\{");

        break;

      case '}':
        pattern.append("\\}");

        break;

      case '[':
        pattern.append("\\[");

        break;

      case ']':
        pattern.append("\\]");

        break;

      case '|':
        pattern.append("\\|");

        break;

      case '.':
        pattern.append("\\.");

        break;

      case '\\':
        pattern.append("\\\\");

        break;

      default:
        pattern.append(c);
    }
  }

  private static String createStringQuery(SearchRequest request) {
    String query = request.getQuery().trim();

    StringBuilder pattern = new StringBuilder();

    if (request.isIgnoreCase()) {
      pattern.append("(?i)");
      query = query.toLowerCase(Locale.ENGLISH);
    }

    pattern.append(".*");

    for (char c : query.toCharArray()) {
      appendChar(pattern, c);
    }

    pattern.append(".*");

    LOG.trace("converted query \"{}\" to regex pattern \"{}\"",
      request.getQuery(), pattern);

    return pattern.toString();
  }
}
