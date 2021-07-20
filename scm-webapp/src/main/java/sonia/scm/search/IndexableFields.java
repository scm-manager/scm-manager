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

}
