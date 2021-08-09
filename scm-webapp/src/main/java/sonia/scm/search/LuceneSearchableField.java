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

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.flexible.standard.config.PointsConfig;

import java.lang.reflect.Field;

@Getter
class LuceneSearchableField implements SearchableField {

  private final String name;
  private final Class<?> type;
  private final ValueExtractor valueExtractor;
  private final float boost;
  private final boolean defaultQuery;
  private final boolean highlighted;
  private final PointsConfig pointsConfig;
  private final Indexed.Analyzer analyzer;

  LuceneSearchableField(Field field, Indexed indexed) {
    this.name = name(field, indexed);
    this.type = field.getType();
    this.valueExtractor = ValueExtractors.create(name, type);
    this.boost = indexed.boost();
    this.defaultQuery = indexed.defaultQuery();
    this.highlighted = indexed.highlighted();
    this.pointsConfig = IndexableFields.pointConfig(field);
    this.analyzer = indexed.analyzer();
  }

  Object value(Document document) {
    return valueExtractor.extract(document);
  }

  private String name(Field field, Indexed indexed) {
    String nameFromAnnotation = indexed.name();
    if (!Strings.isNullOrEmpty(nameFromAnnotation)) {
      return nameFromAnnotation;
    }
    return field.getName();
  }

}
