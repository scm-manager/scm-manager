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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SubjectAware(value = "trillian", permissions = "abc")
@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class LuceneQueryBuilderTest {

  private Directory directory;

  @Mock
  private IndexOpener opener;

  @BeforeEach
  void setUpDirectory() {
    directory = new ByteBuffersDirectory();
  }

  @Test
  void shouldReturnHitsForBestGuessQuery() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(inetOrgPersonDoc("Arthur", "Dent", "Arthur Dent", "4211"));
    }

    QueryResult result = query(InetOrgPerson.class, "Arthur");
    assertThat(result.totalHits()).isOne();
  }

  @Test
  void shouldSupportFieldsFromParentClass() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(inetOrgPersonDoc("Arthur", "Dent", "Arthur Dent", "4211"));
    }

    QueryResult result = query(InetOrgPerson.class, "Dent");
    assertThat(result.totalHits()).isOne();
  }

  @Test
  void shouldIgnoreHitsOfOtherType() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(inetOrgPersonDoc("Arthur", "Dent", "Arthur Dent", "4211"));
      writer.addDocument(personDoc("Dent"));
    }

    QueryResult result = query(InetOrgPerson.class, "Dent");
    assertThat(result.totalHits()).isOne();
  }


  @Test
  void shouldIgnoreNonDefaultFieldsForBestGuessQuery() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(inetOrgPersonDoc("Arthur", "Dent", "Arthur Dent", "car"));
    }

    QueryResult result = query(InetOrgPerson.class, "car");
    assertThat(result.totalHits()).isZero();
  }

  @Test
  void shouldUseBoostFromAnnotationForBestGuessQuery() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(inetOrgPersonDoc("Arthur", "Dent", "Arti", "car"));
      writer.addDocument(inetOrgPersonDoc("Fake", "Dent", "Arthur, Arthur, Arthur", "mycar"));
    }

    QueryResult result = query(InetOrgPerson.class, "Arthur");
    assertThat(result.totalHits()).isEqualTo(2);

    List<Hit> hits = result.hits();
    Hit arthur = hits.get(0);
    assertThat(arthur.get("firstName")).isEqualTo("Arthur");

    Hit fake = hits.get(1);
    assertThat(fake.get("firstName")).isEqualTo("Fake");

    assertThat(arthur.getScore()).isGreaterThan(fake.getScore());
  }

  @Test
  void shouldReturnHitsForExpertQuery() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(simpleDoc("Awesome content one"));
      writer.addDocument(simpleDoc("Awesome content two"));
      writer.addDocument(simpleDoc("Awesome content three"));
    }

    QueryResult result = query(String.class, "content:awesome");
    assertThat(result.totalHits()).isEqualTo(3L);
    assertThat(result.hits()).hasSize(3);
  }

  @Test
  void shouldReturnOnlyHitsOfTypeForExpertQuery() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(inetOrgPersonDoc("Ford", "Prefect", "Ford Prefect", "4211"));
      writer.addDocument(personDoc("Prefect"));
    }

    QueryResult result = query(InetOrgPerson.class, "lastName:prefect");
    assertThat(result.totalHits()).isEqualTo(1L);
  }

  @Test
  void shouldReturnOnlyPermittedHits() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(permissionDoc("Awesome content one", "abc"));
      writer.addDocument(permissionDoc("Awesome content two", "cde"));
      writer.addDocument(permissionDoc("Awesome content three", "fgh"));
    }

    QueryResult result = query(String.class, "content:awesome");
    assertThat(result.totalHits()).isOne();

    List<Hit> hits = result.hits();
    assertThat(hits).hasSize(1).allSatisfy(hit -> {
      assertThat(hit.get("content")).isEqualTo("Awesome content one");
      assertThat(hit.get(Fields.FIELD_PERMISSION)).isEqualTo("abc");
      assertThat(hit.getScore()).isGreaterThan(0f);
    });
  }

  @Test
  void shouldFilterByRepository() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(repositoryDoc("Awesome content one", "abc"));
      writer.addDocument(repositoryDoc("Awesome content two", "cde"));
      writer.addDocument(repositoryDoc("Awesome content three", "fgh"));
    }

    QueryResult result;
    try (DirectoryReader reader = DirectoryReader.open(directory)) {
      when(opener.openForRead("default")).thenReturn(reader);
      LuceneQueryBuilder builder = new LuceneQueryBuilder(
        opener, "default", new StandardAnalyzer()
      );
      result = builder.repository("cde").execute(String.class, "content:awesome");
    }

    assertThat(result.totalHits()).isOne();

    List<Hit> hits = result.hits();
    assertThat(hits).hasSize(1).allSatisfy(hit -> {
      assertThat(hit.get("content")).isEqualTo("Awesome content two");
      assertThat(hit.get(Fields.FIELD_REPOSITORY)).isEqualTo("cde");
      assertThat(hit.getScore()).isGreaterThan(0f);
    });
  }

  @Test
  void shouldSupportIntRangeQueries() throws IOException {
    Instant now = Instant.now();
    try (IndexWriter writer = writer()) {
      writer.addDocument(typesDoc(42, 21L, false, now));
    }

    QueryResult result = query(Types.class, "intValue:[0 TO 100]");
    assertThat(result.totalHits()).isOne();
  }

  @Test
  void shouldSupportLongRangeQueries() throws IOException {
    Instant now = Instant.now();
    try (IndexWriter writer = writer()) {
      writer.addDocument(typesDoc(42, 21L, false, now));
    }

    QueryResult result = query(Types.class, "longValue:[0 TO 100]");
    assertThat(result.totalHits()).isOne();
  }

  @Test
  void shouldSupportInstantRangeQueries() throws IOException {
    Instant now = Instant.now();
    try (IndexWriter writer = writer()) {
      writer.addDocument(typesDoc(42, 21L, false, now));
    }
    long before = now.minus(1, ChronoUnit.MINUTES).toEpochMilli();
    long after = now.plus(1, ChronoUnit.MINUTES).toEpochMilli();

    String queryString = String.format("instantValue:[%d TO %d]", before, after);

    QueryResult result = query(Types.class, queryString);
    assertThat(result.totalHits()).isOne();
  }

  @Test
  void shouldSupportQueryForBooleanFields() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(typesDoc(21, 42L, true, Instant.now()));
    }

    QueryResult result = query(Types.class, "boolValue:true");
    assertThat(result.totalHits()).isOne();
  }

  @Test
  void shouldBeAbleToMarshalQueryResultToJson() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(inetOrgPersonDoc("Arthur", "Dent", "Arthur Dent", "4211"));
    }

    QueryResult result = query(InetOrgPerson.class, "Arthur");
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(System.out, result);
  }

  private QueryResult query(Class<?> type, String queryString) throws IOException {
    try (DirectoryReader reader = DirectoryReader.open(directory)) {
      when(opener.openForRead("default")).thenReturn(reader);
      LuceneQueryBuilder builder = new LuceneQueryBuilder(
        opener, "default", new StandardAnalyzer()
      );
      return builder.execute(type, queryString);
    }
  }

  private IndexWriter writer() throws IOException {
    IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    return new IndexWriter(directory, config);
  }

  private Document simpleDoc(String content) {
    Document document = new Document();
    document.add(new TextField("content", content, Field.Store.YES));
    document.add(new StringField(Fields.FIELD_TYPE, "java.lang.String", Field.Store.YES));
    return document;
  }

  private Document permissionDoc(String content, String permission) {
    Document document = new Document();
    document.add(new TextField("content", content, Field.Store.YES));
    document.add(new StringField(Fields.FIELD_TYPE, "java.lang.String", Field.Store.YES));
    document.add(new StringField(Fields.FIELD_PERMISSION, permission, Field.Store.YES));
    return document;
  }

  private Document repositoryDoc(String content, String repository) {
    Document document = new Document();
    document.add(new TextField("content", content, Field.Store.YES));
    document.add(new StringField(Fields.FIELD_TYPE, "java.lang.String", Field.Store.YES));
    document.add(new StringField(Fields.FIELD_REPOSITORY, repository, Field.Store.YES));
    return document;
  }

  private Document inetOrgPersonDoc(String firstName, String lastName, String displayName, String carLicense) {
    Document document = new Document();
    document.add(new TextField("firstName", firstName, Field.Store.YES));
    document.add(new TextField("lastName", lastName, Field.Store.YES));
    document.add(new TextField("displayName", displayName, Field.Store.YES));
    document.add(new TextField("carLicense", carLicense, Field.Store.YES));
    document.add(new StringField(Fields.FIELD_TYPE, InetOrgPerson.class.getName(), Field.Store.YES));
    return document;
  }

  private Document personDoc(String lastName) {
    Document document = new Document();
    document.add(new TextField("lastName", lastName, Field.Store.YES));
    document.add(new StringField(Fields.FIELD_TYPE, Person.class.getName(), Field.Store.YES));
    return document;
  }

  private Document typesDoc(int intValue, long longValue, boolean boolValue, Instant instantValue) {
    Document document = new Document();
    document.add(new IntPoint("intValue", intValue));
    document.add(new StoredField("intValue", intValue));
    document.add(new LongPoint("longValue", longValue));
    document.add(new StoredField("longValue", longValue));
    document.add(new StringField("boolValue", String.valueOf(boolValue), Field.Store.YES));
    document.add(new LongPoint("instantValue", instantValue.toEpochMilli()));
    document.add(new StoredField("instantValue", instantValue.toEpochMilli()));
    document.add(new StringField(Fields.FIELD_TYPE, Types.class.getName(), Field.Store.YES));
    return document;
  }

  static class Types {

    @Indexed
    private Integer intValue;
    @Indexed
    private long longValue;
    @Indexed
    private boolean boolValue;
    @Indexed
    private Instant instantValue;

  }

  static class Person {

    @Indexed(defaultQuery = true)
    private String lastName;
  }

  static class InetOrgPerson extends Person {

    @Indexed(defaultQuery = true, boost = 2f)
    private String firstName;

    @Indexed(defaultQuery = true)
    private String displayName;

    @Indexed
    private String carLicense;
  }

}
