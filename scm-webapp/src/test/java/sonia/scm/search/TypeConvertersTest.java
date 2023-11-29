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

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TypeConvertersTest {

  @Test
  void shouldConvertPersonToDocument() {
    Person person = new Person("Arthur", "Dent");

    Document document = convert(person);

    assertThat(document.getField("firstName").stringValue()).isEqualTo("Arthur");
    assertThat(document.getField("lastName").stringValue()).isEqualTo("Dent");
  }

  @Nonnull
  private Document convert(Object object) {
    return TypeConverters.create(object.getClass()).convert(object);
  }

  @Test
  void shouldUseNameFromAnnotation() {
    Document document = convert(new ParamSample());

    assertThat(document.getField("username").stringValue()).isEqualTo("dent");
  }

  @Test
  void shouldBeIndexedAsTextFieldByDefault() {
    Document document = convert(new ParamSample());

    assertThat(document.getField("username")).isInstanceOf(TextField.class);
  }

  @Test
  void shouldBeIndexedAsStringField() {
    Document document = convert(new ParamSample());

    assertThat(document.getField("searchable")).isInstanceOf(StringField.class);
  }

  @Test
  void shouldBeIndexedAsStoredField() {
    Document document = convert(new ParamSample());

    assertThat(document.getField("storedOnly")).isInstanceOf(StoredField.class);
  }

  @Test
  void shouldIgnoreNonIndexedFields() {
    Document document = convert(new ParamSample());

    assertThat(document.getField("notIndexed")).isNull();
  }

  @Test
  void shouldSupportInheritance() {
    Account account = new Account("Arthur", "Dent", "arthur@hitchhiker.com");

    Document document = convert(account);

    assertThat(document.getField("firstName")).isNotNull();
    assertThat(document.getField("lastName")).isNotNull();
    assertThat(document.getField("mail")).isNotNull();
  }

  @Test
  void shouldFailWithoutGetter() {
    WithoutGetter withoutGetter = new WithoutGetter();
    assertThrows(NonReadableFieldException.class, () -> convert(withoutGetter));
  }

  @Test
  void shouldFailOnUnsupportedFieldType() {
    UnsupportedFieldType unsupportedFieldType = new UnsupportedFieldType();
    assertThrows(UnsupportedTypeOfFieldException.class, () -> convert(unsupportedFieldType));
  }

  @Test
  void shouldStoreLongFieldsAsPointAndStoredByDefault() {
    Document document = convert(new SupportedTypes());

    assertPointField(document, "longType",
      field -> assertThat(field.numericValue().longValue()).isEqualTo(42L)
    );
  }

  @Test
  void shouldStoreLongFieldAsStored() {
    Document document = convert(new SupportedTypes());

    IndexableField field = document.getField("storedOnlyLongType");
    assertThat(field).isInstanceOf(StoredField.class);
    assertThat(field.numericValue().longValue()).isEqualTo(42L);
  }

  @Test
  void shouldStoreIntegerFieldsAsPointAndStoredByDefault() {
    Document document = convert(new SupportedTypes());

    assertPointField(document, "intType",
      field -> assertThat(field.numericValue().intValue()).isEqualTo(42)
    );
  }

  @Test
  void shouldStoreIntegerFieldAsStored() {
    Document document = convert(new SupportedTypes());

    IndexableField field = document.getField("storedOnlyIntegerType");
    assertThat(field).isInstanceOf(StoredField.class);
    assertThat(field.numericValue().intValue()).isEqualTo(42);
  }

  @Test
  void shouldStoreBooleanFieldsAsStringField() {
    Document document = convert(new SupportedTypes());

    IndexableField field = document.getField("boolType");
    assertThat(field).isInstanceOf(StringField.class);
    assertThat(field.stringValue()).isEqualTo("true");
    assertThat(field.fieldType().stored()).isTrue();
  }

  @Test
  void shouldStoreBooleanFieldAsStored() {
    Document document = convert(new SupportedTypes());

    IndexableField field = document.getField("storedOnlyBoolType");
    assertThat(field).isInstanceOf(StoredField.class);
    assertThat(field.stringValue()).isEqualTo("true");
  }

  @Test
  void shouldStoreInstantFieldsAsPointAndStoredByDefault() {
    Instant now = Instant.now();
    Document document = convert(new DateTypes(now));

    assertPointField(document, "instant",
      field -> assertThat(field.numericValue().longValue()).isEqualTo(now.toEpochMilli())
    );
  }

  @Test
  void shouldStoreInstantFieldAsStored() {
    Instant now = Instant.now();
    Document document = convert(new DateTypes(now));

    IndexableField field = document.getField("storedOnlyInstant");
    assertThat(field).isInstanceOf(StoredField.class);
    assertThat(field.numericValue().longValue()).isEqualTo(now.toEpochMilli());
  }

  @Test
  void shouldCreateNoFieldForNullValues() {
    Document document = convert(new Person("Trillian", null));

    assertThat(document.getField("firstName")).isNotNull();
    assertThat(document.getField("lastName")).isNull();
  }

  @Test
  void shouldCreateEnumField() {
    Document document = convert(new TypeWithEnum(Color.GREEN));

    assertThat(document.get("color")).isEqualTo("GREEN");
  }

  private void assertPointField(Document document, String name, Consumer<IndexableField> consumer) {
    IndexableField[] fields = document.getFields(name);
    assertThat(fields)
      .allSatisfy(consumer)
      .anySatisfy(field -> assertThat(field.fieldType().stored()).isFalse())
      .anySatisfy(field -> assertThat(field.fieldType().stored()).isTrue());
  }

  @Getter
  @IndexedType
  @AllArgsConstructor
  public static class Person {
    @Indexed
    private String firstName;
    @Indexed
    private String lastName;
  }

  @Getter
  @IndexedType
  public static class Account extends Person {
    @Indexed
    private String mail;

    public Account(String firstName, String lastName, String mail) {
      super(firstName, lastName);
      this.mail = mail;
    }
  }

  @Getter
  @IndexedType
  public static class ParamSample {
    @Indexed(name = "username")
    private final String name = "dent";

    @Indexed(type = Indexed.Type.SEARCHABLE)
    private final String searchable = "--";

    @Indexed(type = Indexed.Type.STORED_ONLY)
    private final String storedOnly = "--";

    private final String notIndexed = "--";
  }

  @IndexedType
  public static class WithoutGetter {
    @Indexed
    private final String value = "one";
  }

  @Getter
  @IndexedType
  public static class UnsupportedFieldType {
    @Indexed
    private final Object value = "one";
  }

  @Getter
  @IndexedType
  public static class SupportedTypes {
    @Indexed
    private final Long longType = 42L;
    @Indexed(type = Indexed.Type.STORED_ONLY)
    private final long storedOnlyLongType = 42L;

    @Indexed
    private final int intType = 42;
    @Indexed(type = Indexed.Type.STORED_ONLY)
    private final Integer storedOnlyIntegerType = 42;

    @Indexed
    private final boolean boolType = true;
    @Indexed(type = Indexed.Type.STORED_ONLY)
    private final boolean storedOnlyBoolType = true;
  }

  @Getter
  @IndexedType
  private static class DateTypes {
    @Indexed
    private final Instant instant;

    @Indexed(type = Indexed.Type.STORED_ONLY)
    private final Instant storedOnlyInstant;

    private DateTypes(Instant instant) {
      this.instant = instant;
      this.storedOnlyInstant = instant;
    }
  }

  @Getter
  @IndexedType
  private static class TypeWithEnum {
    @Indexed
    private final Color color;

    private TypeWithEnum(Color color) {
      this.color = color;
    }
  }

  private enum Color {
    GREEN, RED, BLUE, YELLOW
  }
}
