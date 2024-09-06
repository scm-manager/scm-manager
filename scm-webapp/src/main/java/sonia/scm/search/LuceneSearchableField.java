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
  private final boolean searchable;
  private final boolean tokenized;

  LuceneSearchableField(Field field, Indexed indexed) {
    this.name = name(field, indexed);
    this.type = field.getType();
    this.valueExtractor = ValueExtractors.create(name, type);
    this.boost = indexed.boost();
    this.defaultQuery = indexed.defaultQuery();
    this.highlighted = indexed.highlighted();
    this.pointsConfig = IndexableFields.pointConfig(field);
    this.analyzer = indexed.analyzer();
    this.searchable = indexed.type().isSearchable();
    this.tokenized = indexed.type().isTokenized() && String.class.isAssignableFrom(type);
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
