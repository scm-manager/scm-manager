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

import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.flexible.standard.config.PointsConfig;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singleton;
import static sonia.scm.search.TypeCheck.isBoolean;
import static sonia.scm.search.TypeCheck.isInstant;
import static sonia.scm.search.TypeCheck.isInteger;
import static sonia.scm.search.TypeCheck.isLong;

class IndexableFields {
  private IndexableFields() {
  }

  static PointsConfig pointConfig(Field field) {
    Class<?> type = field.getType();
    if (isLong(type) || isInstant(type)) {
      return new PointsConfig(new DecimalFormat(), Long.class);
    } else if (isInteger(type)) {
      return new PointsConfig(new DecimalFormat(), Integer.class);
    }
    return null;
  }

  static IndexableFieldFactory create(Field field, Indexed indexed) {
    Class<?> fieldType = field.getType();
    Indexed.Type indexType = indexed.type();
    if (fieldType == String.class) {
      return new StringFieldFactory(indexType);
    } else if (isLong(fieldType)) {
      return new LongFieldFactory(indexType);
    } else if (isInteger(fieldType)) {
      return new IntegerFieldFactory(indexType);
    } else if (isBoolean(fieldType)) {
      return new BooleanFieldFactory(indexType);
    } else if (isInstant(fieldType)) {
      return new InstantFieldFactory(indexType);
    } else if (fieldType.isEnum()) {
      return new EnumFieldFactory(indexType);
    } else {
      throw new UnsupportedTypeOfFieldException(fieldType, field.getName());
    }
  }

  private static class StringFieldFactory implements IndexableFieldFactory {
    private final Indexed.Type type;

    private StringFieldFactory(Indexed.Type type) {
      this.type = type;
    }

    @Override
    public Iterable<IndexableField> create(String name, Object value) {
      String stringValue = (String) value;
      if (type.isTokenized()) {
        return singleton(new TextField(name, stringValue, Store.YES));
      } else if (type.isSearchable()) {
        return singleton(new StringField(name, stringValue, Store.YES));
      } else {
        return singleton(new StoredField(name, stringValue));
      }
    }
  }

  private static class LongFieldFactory implements IndexableFieldFactory {

    private final Indexed.Type type;

    private LongFieldFactory(Indexed.Type type) {
      this.type = type;
    }

    @Override
    public Iterable<IndexableField> create(String name, Object value) {
      Long longValue = (Long) value;
      List<IndexableField> fields = new ArrayList<>();
      if (type.isSearchable()) {
        fields.add(new LongPoint(name, longValue));
      }
      fields.add(new StoredField(name, longValue));
      return Collections.unmodifiableList(fields);
    }
  }

  private static class IntegerFieldFactory implements IndexableFieldFactory {

    private final Indexed.Type type;

    private IntegerFieldFactory(Indexed.Type type) {
      this.type = type;
    }

    @Override
    public Iterable<IndexableField> create(String name, Object value) {
      Integer integerValue = (Integer) value;
      List<IndexableField> fields = new ArrayList<>();
      if (type.isSearchable()) {
        fields.add(new IntPoint(name, integerValue));
      }
      fields.add(new StoredField(name, integerValue));
      return Collections.unmodifiableList(fields);
    }
  }

  private static class BooleanFieldFactory implements IndexableFieldFactory {
    private final Indexed.Type type;

    private BooleanFieldFactory(Indexed.Type type) {
      this.type = type;
    }

    @Override
    public Iterable<IndexableField> create(String name, Object value) {
      Boolean booleanValue = (Boolean) value;
      if (type.isSearchable()) {
        return singleton(new StringField(name, booleanValue.toString(), Store.YES));
      } else {
        return singleton(new StoredField(name, booleanValue.toString()));
      }
    }
  }

  private static class InstantFieldFactory extends LongFieldFactory {

    private InstantFieldFactory(Indexed.Type type) {
      super(type);
    }

    @Override
    public Iterable<IndexableField> create(String name, Object value) {
      Instant instant = (Instant) value;
      return super.create(name, instant.toEpochMilli());
    }
  }

  private static class EnumFieldFactory implements IndexableFieldFactory {

    private final Indexed.Type type;

    public EnumFieldFactory(Indexed.Type type) {
      this.type = type;
    }

    @Override
    public Iterable<IndexableField> create(String name, Object value) {
      String stringValue = value.toString();
      if (type.isSearchable()) {
        return singleton(new StringField(name, stringValue, Store.YES));
      } else {
        return singleton(new StoredField(name, stringValue));
      }
    }
  }

}
