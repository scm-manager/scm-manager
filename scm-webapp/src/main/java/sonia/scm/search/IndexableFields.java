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
import java.util.Arrays;
import java.util.Date;

import static java.util.Collections.singleton;

class IndexableFields {
  private IndexableFields() {
  }

  static PointsConfig pointConfig(Field field) {
    Class<?> type = field.getType();
    if (isLong(type) || isDate(type) || isInstant(type)) {
      return new PointsConfig(new DecimalFormat(), Long.class);
    } else if (isInteger(type)) {
      return new PointsConfig(new DecimalFormat(), Integer.class);
    }
    return null;
  }

  static IndexableFieldFactory create(Field field, Indexed indexed) {
    Class<?> type = field.getType();
    if (type == String.class) {
      return new StringFieldFactory(indexed);
    } else if (isLong(type)) {
      return createStorableField(indexed, LONG_STORED_FIELD_FACTORY, LONG_FIELD_FACTORY);
    } else if (isInteger(type)) {
      return createStorableField(indexed, INTEGER_STORED_FIELD_FACTORY, INTEGER_FIELD_FACTORY);
    } else if (isBoolean(type)) {
      if (indexed.stored() == Stored.NO) {
        return BOOLEAN_NOT_STORED_FIELD_FACTORY;
      } else {
        return BOOLEAN_FIELD_FACTORY;
      }
    } else if (isDate(type)) {
      return createStorableField(indexed, DATE_STORED_FIELD_FACTORY, DATE_FIELD_FACTORY);
    } else if (isInstant(type)) {
      return createStorableField(indexed, INSTANT_STORED_FIELD_FACTORY, INSTANT_FIELD_FACTORY);
    } else {
      throw new UnsupportedTypeOfFieldException(
        "type " + type + " of " + field.getName() + " is unsupported."
      );
    }
  }

  private static boolean isLong(Class<?> type) {
    return type == Long.TYPE || type == Long.class;
  }

  private static boolean isInteger(Class<?> type) {
    return type == Integer.TYPE || type == Integer.class;
  }

  private static boolean isBoolean(Class<?> type) {
    return type == Boolean.TYPE || type == Boolean.class;
  }

  private static boolean isDate(Class<?> type) {
    return type == Date.class;
  }

  private static boolean isInstant(Class<?> type) {
    return type == Instant.class;
  }

  private static IndexableFieldFactory createStorableField(Indexed indexed, IndexableFieldFactory storableFactory, IndexableFieldFactory factory) {
    if (indexed.stored() == Stored.YES) {
      return storableFactory;
    } else {
      return factory;
    }
  }

  private static class StringFieldFactory implements IndexableFieldFactory {

    private final boolean tokenized;
    private final Store store;

    private StringFieldFactory(Indexed indexed) {
      this.tokenized = indexed.tokenized();
      this.store = createStore(indexed.stored());
    }

    private Store createStore(Stored stored) {
      if (stored == Stored.NO) {
        return Store.NO;
      }
      return Store.YES;
    }

    @Override
    public Iterable<IndexableField> create(String name, Object value) {
      String stringValue = (String) value;
      if (tokenized) {
        return singleton(new TextField(name, stringValue, store));
      }
      return singleton(new StringField(name, stringValue, store));
    }
  }

  private static final IndexableFieldFactory LONG_FIELD_FACTORY = (name, value) -> singleton(
    new LongPoint(name, (Long) value)
  );

  private static final IndexableFieldFactory LONG_STORED_FIELD_FACTORY = (name, value) -> Arrays.asList(
    new LongPoint(name, (Long) value), new StoredField(name, (Long) value)
  );

  private static final IndexableFieldFactory INTEGER_FIELD_FACTORY = (name, value) -> singleton(
    new IntPoint(name, (Integer) value)
  );

  private static final IndexableFieldFactory INTEGER_STORED_FIELD_FACTORY = (name, value) -> Arrays.asList(
    new IntPoint(name, (Integer) value), new StoredField(name, (Integer) value)
  );

  private static final IndexableFieldFactory BOOLEAN_FIELD_FACTORY = (name, value) -> singleton(
    new StringField(name, value.toString(), Store.YES)
  );

  private static final IndexableFieldFactory BOOLEAN_NOT_STORED_FIELD_FACTORY = (name, value) -> singleton(
    new StringField(name, value.toString(), Store.NO)
  );

  private static final IndexableFieldFactory DATE_FIELD_FACTORY = (name, value) -> singleton(
    new LongPoint(name, ((Date) value).getTime())
  );

  private static final IndexableFieldFactory DATE_STORED_FIELD_FACTORY = (name, value) -> {
    long time = ((Date) value).getTime();
    return Arrays.asList(
      new LongPoint(name, time),
      new StoredField(name, time)
    );
  };

  private static final IndexableFieldFactory INSTANT_FIELD_FACTORY = (name, value) -> singleton(
    new LongPoint(name, ((Instant) value).toEpochMilli())
  );

  private static final IndexableFieldFactory INSTANT_STORED_FIELD_FACTORY = (name, value) -> {
    long time = ((Instant) value).toEpochMilli();
    return Arrays.asList(
      new LongPoint(name, time),
      new StoredField(name, time)
    );
  };

}
