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

import org.apache.lucene.queryparser.flexible.standard.config.PointsConfig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class SearchableTypes {

  private SearchableTypes() {
  }

  static SearchableType create(Class<?> type) {
    List<SearchableField> fields = new ArrayList<>();
    collectFields(type, fields);
    return createSearchableType(type, fields);
  }

  private static SearchableType createSearchableType(Class<?> type, List<SearchableField> fields) {
    String[] fieldsNames = fields.stream()
      .filter(SearchableField::isDefaultQuery)
      .map(SearchableField::getName)
      .toArray(String[]::new);

    Map<String, Float> boosts = new HashMap<>();
    Map<String, PointsConfig> pointsConfig = new HashMap<>();
    for (SearchableField field : fields) {
      if (field.isDefaultQuery() && field.getBoost() != 1f) {
        boosts.put(field.getName(),  field.getBoost());
      }
      PointsConfig config = field.getPointsConfig();
      if (config != null) {
        pointsConfig.put(field.getName(), config);
      }
    }

    return new SearchableType(type, fieldsNames, boosts, pointsConfig, fields);
  }


  private static void collectFields(Class<?> type, List<SearchableField> fields) {
    Class<?> parent = type.getSuperclass();
    if (parent != null) {
      collectFields(parent, fields);
    }
    for (Field field : type.getDeclaredFields()) {
      Indexed indexed = field.getAnnotation(Indexed.class);
      if (indexed != null) {
        fields.add(new SearchableField(field, indexed));
      }
    }
  }



}
