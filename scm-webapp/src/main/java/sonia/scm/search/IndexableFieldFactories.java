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

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;

import static java.util.Collections.singleton;

class IndexableFieldFactories {
  private IndexableFieldFactories() {}

  static IndexableFieldFactory create(Field field, Indexed indexed) {
    Class<?> type = field.getType();
    if (type == String.class) {
      return new StringFieldFactory(indexed);
    } else if (type == Long.TYPE) {
      return createStorableField(indexed, LONG_STORED_FIELD_FACTORY, LONG_FIELD_FACTORY);
    } else if (type == Integer.TYPE) {
      return createStorableField(indexed, INTEGER_STORED_FIELD_FACTORY, INTEGER_FIELD_FACTORY);
    } else if (type == Boolean.TYPE) {
      return BOOLEAN_FIELD_FACTORY;
    } else if (type == Date.class) {
      return createStorableField(indexed, DATE_STORED_FIELD_FACTORY, DATE_FIELD_FACTORY);
    } else if (type == Instant.class) {
      return createStorableField(indexed, INSTANT_STORED_FIELD_FACTORY, INSTANT_FIELD_FACTORY);
    } else {
      throw new UnsupportedTypeOfFieldException(
        "type " + type + " of " + field.getName() + " is unsupported."
      );
    }
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
    new LongPoint(name, (Long) value), new StoredField("__" + name, (Long) value)
  );

  private static final IndexableFieldFactory INTEGER_FIELD_FACTORY = (name, value) -> singleton(
    new IntPoint(name, (Integer) value)
  );

  private static final IndexableFieldFactory INTEGER_STORED_FIELD_FACTORY = (name, value) -> Arrays.asList(
    new IntPoint(name, (Integer) value), new StoredField("__" + name, (Integer) value)
  );

  private static final IndexableFieldFactory BOOLEAN_FIELD_FACTORY = (name, value) -> singleton(
    new StoredField(name, value.toString())
  );

  private static final IndexableFieldFactory DATE_FIELD_FACTORY = (name, value) -> singleton(
    new LongPoint(name, ((Date) value).getTime())
  );

  private static final IndexableFieldFactory DATE_STORED_FIELD_FACTORY = (name, value) -> {
    long time = ((Date) value).getTime();
    return Arrays.asList(
      new LongPoint(name, time), new StoredField("__" + name, time)
    );
  };

  private static final IndexableFieldFactory INSTANT_FIELD_FACTORY = (name, value) -> singleton(
    new LongPoint(name, ((Instant) value).toEpochMilli())
  );

  private static final IndexableFieldFactory INSTANT_STORED_FIELD_FACTORY = (name, value) -> {
    long time = ((Instant) value).toEpochMilli();
    return Arrays.asList(
      new LongPoint(name, time), new StoredField("__" + name, time)
    );
  };

}
