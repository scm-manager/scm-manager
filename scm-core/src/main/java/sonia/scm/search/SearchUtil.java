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
