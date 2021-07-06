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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexableFieldType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentConverterTest {

  private DocumentConverter documentConverter;

  @BeforeEach
  void prepare() {
    documentConverter = new DocumentConverter();
  }

  @Test
  void shouldConvertPersonToDocument() {
    Person person = new Person("Arthur", "Dent");

    Document document = documentConverter.convert(person);

    assertThat(document.getField("firstName").stringValue()).isEqualTo("Arthur");
    assertThat(document.getField("lastName").stringValue()).isEqualTo("Dent");
  }

  @Test
  void shouldUseNameFromAnnotation() {
    Document document = documentConverter.convert(new ParamSample());

    assertThat(document.getField("username").stringValue()).isEqualTo("dent");
  }

  @Test
  void shouldIndexStoredAndTokenizedByDefault() {
    Document document = documentConverter.convert(new ParamSample());

    IndexableFieldType fieldType = document.getField("username").fieldType();
    assertThat(fieldType.stored()).isTrue();
    assertThat(fieldType.tokenized()).isTrue();
  }

  @Test
  void shouldRespectStoredParameter() {
    Document document = documentConverter.convert(new ParamSample());

    assertThat(document.getField("notStored").fieldType().stored()).isFalse();
  }

  @Test
  void shouldRespectTokenizedParameter() {
    Document document = documentConverter.convert(new ParamSample());

    assertThat(document.getField("notTokenized").fieldType().tokenized()).isFalse();
  }

  @Test
  void shouldIgnoreNonIndexedFields() {
    Document document = documentConverter.convert(new ParamSample());

    assertThat(document.getField("notIndexed")).isNull();
  }

  @Test
  void shouldSupportInheritance() {
    Account account = new Account("Arthur", "Dent", "arthur@hitchhiker.com");

    Document document = documentConverter.convert(account);

    assertThat(document.getField("firstName")).isNotNull();
    assertThat(document.getField("lastName")).isNotNull();
    assertThat(document.getField("mail")).isNotNull();
  }

  @Test
  void shouldFailWithoutGetter() {
    WithoutGetter withoutGetter = new WithoutGetter();
    assertThrows(NonReadableFieldException.class, () -> documentConverter.convert(withoutGetter));
  }

  @Test
  void shouldFailOnUnsupportedFieldType() {
    UnsupportedFieldType unsupportedFieldType = new UnsupportedFieldType();
    assertThrows(UnsupportedTypeOfFieldException.class, () -> documentConverter.convert(unsupportedFieldType));
  }

  @Test
  void shouldSupportLongFields() {
    Document document = documentConverter.convert(new SupportedTypes());

    IndexableField longType = document.getField("longType");
    assertThat(longType.numericValue()).isEqualTo(42L);
    assertThat(longType.fieldType().stored()).isFalse();
  }

  @Test
  void shouldCreateSeparateFieldForStoredLongValues() {
    Document document = documentConverter.convert(new SupportedTypes());
    assertPointField(document, "storedLongType", field -> {
      assertThat(field.numericValue().longValue()).isEqualTo(42L);
    });
  }

  private void assertPointField(Document document, String name, Consumer<IndexableField> consumer) {
    IndexableField[] fields = document.getFields(name);
    assertThat(fields)
      .allSatisfy(consumer)
      .anySatisfy(field -> assertThat(field.fieldType().stored()).isFalse())
      .anySatisfy(field -> assertThat(field.fieldType().stored()).isTrue());
  }

  @Test
  void shouldSupportIntFields() {
    Document document = documentConverter.convert(new SupportedTypes());

    IndexableField longType = document.getField("intType");
    assertThat(longType.numericValue()).isEqualTo(42);
    assertThat(longType.fieldType().stored()).isFalse();
  }

  @Test
  void shouldCreateSeparateFieldForStoredIntValues() {
    Document document = documentConverter.convert(new SupportedTypes());
    assertPointField(document, "storedIntType", field -> {
      assertThat(field.numericValue().intValue()).isEqualTo(42);
    });
  }

  @Test
  void shouldSupportBooleanFields() {
    Document document = documentConverter.convert(new SupportedTypes());

    IndexableField longType = document.getField("boolType");
    assertThat(longType.stringValue()).isEqualTo("true");
  }

  @Test
  void shouldSupportDateFields() {
    Instant now = Instant.now();
    Document document = documentConverter.convert(new DateTypes(now));

    IndexableField field = document.getField("date");
    assertThat(field.numericValue()).isEqualTo(now.toEpochMilli());
  }

  @Test
  void shouldCreateSeparateFieldForStoredDateValues() {
    Instant now = Instant.now();
    Document document = documentConverter.convert(new DateTypes(now));
    assertPointField(document, "storedDate", field -> {
      assertThat(field.numericValue().longValue()).isEqualTo(now.toEpochMilli());
    });
  }

  @Test
  void shouldSupportInstantFields() {
    Instant now = Instant.now();
    Document document = documentConverter.convert(new DateTypes(now));

    IndexableField field = document.getField("instant");
    assertThat(field.numericValue()).isEqualTo(now.toEpochMilli());
  }

  @Test
  void shouldCreateSeparateFieldForStoredInstantValues() {
    Instant now = Instant.now();
    Document document = documentConverter.convert(new DateTypes(now));
    assertPointField(document, "storedInstant", field -> {
      assertThat(field.numericValue().longValue()).isEqualTo(now.toEpochMilli());
    });
  }

  @Getter
  @AllArgsConstructor
  public static class Person {
    @Indexed
    private String firstName;
    @Indexed
    private String lastName;
  }

  @Getter
  public static class Account extends Person {
    @Indexed
    private String mail;

    public Account(String firstName, String lastName, String mail) {
      super(firstName, lastName);
      this.mail = mail;
    }
  }

  @Getter
  public static class ParamSample {
    @Indexed(name = "username")
    private final String name = "dent";

    @Indexed(stored = Stored.NO)
    private final String notStored = "--";

    @Indexed(tokenized = false)
    private final String notTokenized = "--";

    private final String notIndexed = "--";
  }

  public static class WithoutGetter {
    @Indexed
    private final String value = "one";
  }

  @Getter
  public static class UnsupportedFieldType {
    @Indexed
    private final Object value = "one";
  }

  @Getter
  public static class SupportedTypes {
    @Indexed
    private final Long longType = 42L;
    @Indexed(stored = Stored.YES)
    private final long storedLongType = 42L;
    @Indexed
    private final int intType = 42;
    @Indexed(stored = Stored.YES)
    private final Integer storedIntType = 42;
    @Indexed
    private final boolean boolType = true;
  }

  @Getter
  private static class DateTypes {
    @Indexed
    private final Instant instant;

    @Indexed(stored = Stored.YES)
    private final Instant storedInstant;

    @Indexed
    private final Date date;

    @Indexed(stored = Stored.YES)
    private final Date storedDate;

    private DateTypes(Instant instant) {
      this.instant = instant;
      this.storedInstant = instant;
      this.date = Date.from(instant);
      this.storedDate = Date.from(instant);
    }
  }
}
