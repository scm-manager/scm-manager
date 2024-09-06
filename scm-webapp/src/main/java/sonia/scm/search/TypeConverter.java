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

import jakarta.annotation.Nonnull;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

final class TypeConverter {

  private final List<FieldConverter> fieldConverters;

  TypeConverter(List<FieldConverter> fieldConverters) {
    this.fieldConverters = fieldConverters;
  }

  public Document convert(Object object) {
    try {
      return doConversion(object);
    } catch (IllegalAccessException | InvocationTargetException ex) {
      throw new SearchEngineException("failed to create document", ex);
    }
  }

  @Nonnull
  private Document doConversion(Object object) throws IllegalAccessException, InvocationTargetException {
    Document document = new Document();
    for (FieldConverter fieldConverter : fieldConverters) {
      for (IndexableField field : fieldConverter.convert(object)) {
        document.add(field);
      }
    }
    return document;
  }

}
