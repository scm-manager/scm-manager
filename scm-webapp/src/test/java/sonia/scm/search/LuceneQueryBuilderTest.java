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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
    assertThat(result.getTotalHits()).isOne();
  }

  @Test
  void shouldSupportFieldsFromParentClass() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(inetOrgPersonDoc("Arthur", "Dent", "Arthur Dent", "4211"));
    }

    QueryResult result = query(InetOrgPerson.class, "Dent");
    assertThat(result.getTotalHits()).isOne();
  }

  @Test
  void shouldIgnoreHitsOfOtherType() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(inetOrgPersonDoc("Arthur", "Dent", "Arthur Dent", "4211"));
      writer.addDocument(personDoc("Dent"));
    }

    QueryResult result = query(InetOrgPerson.class, "Dent");
    assertThat(result.getTotalHits()).isOne();
  }

  @Test
  void shouldIgnoreNonDefaultFieldsForBestGuessQuery() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(inetOrgPersonDoc("Arthur", "Dent", "Arthur Dent", "car"));
    }

    QueryResult result = query(InetOrgPerson.class, "car");
    assertThat(result.getTotalHits()).isZero();
  }

  @Test
  void shouldUseBoostFromAnnotationForBestGuessQuery() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(inetOrgPersonDoc("Arthur", "Dent", "Arti", "car"));
      writer.addDocument(inetOrgPersonDoc("Fake", "Dent", "Arthur, Arthur, Arthur", "mycar"));
    }

    QueryResult result = query(InetOrgPerson.class, "Arthur");
    assertThat(result.getTotalHits()).isEqualTo(2);

    List<Hit> hits = result.getHits();
    Hit arthur = hits.get(0);
    assertThat(arthur.getFields()).containsEntry("firstName", "Arthur");

    Hit fake = hits.get(1);
    assertThat(fake.getFields()).containsEntry("firstName", "Fake");

    assertThat(arthur.getScore()).isGreaterThan(fake.getScore());
  }

  @Test
  void shouldReturnHitsForExpertQuery() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(simpleDoc("Awesome content one"));
      writer.addDocument(simpleDoc("Awesome content two"));
      writer.addDocument(simpleDoc("Awesome content three"));
    }

    QueryResult result = query(Simple.class, "content:awesome");
    assertThat(result.getTotalHits()).isEqualTo(3L);
    assertThat(result.getHits()).hasSize(3);
  }

  @Test
  void shouldReturnOnlyHitsOfTypeForExpertQuery() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(inetOrgPersonDoc("Ford", "Prefect", "Ford Prefect", "4211"));
      writer.addDocument(personDoc("Prefect"));
    }

    QueryResult result = query(InetOrgPerson.class, "lastName:prefect");
    assertThat(result.getTotalHits()).isEqualTo(1L);
  }

  @Test
  void shouldReturnOnlyPermittedHits() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(permissionDoc("Awesome content one", "abc"));
      writer.addDocument(permissionDoc("Awesome content two", "cde"));
      writer.addDocument(permissionDoc("Awesome content three", "fgh"));
    }

    QueryResult result = query(Simple.class, "content:awesome");
    assertThat(result.getTotalHits()).isOne();

    List<Hit> hits = result.getHits();
    assertThat(hits).hasSize(1).allSatisfy(hit -> {
      assertThat(hit.getFields()).containsEntry("content", "Awesome content one");
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
      result = builder.repository("cde").execute(Simple.class, "content:awesome");
    }

    assertThat(result.getTotalHits()).isOne();

    List<Hit> hits = result.getHits();
    assertThat(hits).hasSize(1).allSatisfy(hit -> {
      assertThat(hit.getFields()).containsEntry("content", "Awesome content two");
      assertThat(hit.getScore()).isGreaterThan(0f);
    });
  }

  @Test
  void shouldReturnStringFields() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(simpleDoc("Awesome"));
    }

    QueryResult result = query(Simple.class, "content:awesome");
    assertThat(result.getTotalHits()).isOne();
    assertThat(result.getHits()).allSatisfy(hit -> {
      assertThat(hit.getFields()).containsEntry("content", "Awesome");
    });
  }

  @Test
  void shouldSupportIntRangeQueries() throws IOException {
    Instant now = Instant.now();
    try (IndexWriter writer = writer()) {
      writer.addDocument(typesDoc(42, 21L, false, now));
    }

    QueryResult result = query(Types.class, "intValue:[0 TO 100]");
    assertThat(result.getTotalHits()).isOne();
    assertThat(result.getHits()).allSatisfy(hit -> {
      assertThat(hit.getFields()).containsEntry("intValue", 42);
    });
  }

  @Test
  void shouldSupportLongRangeQueries() throws IOException {
    Instant now = Instant.now();
    try (IndexWriter writer = writer()) {
      writer.addDocument(typesDoc(42, 21L, false, now));
    }

    QueryResult result = query(Types.class, "longValue:[0 TO 100]");
    assertThat(result.getTotalHits()).isOne();
    assertThat(result.getHits()).allSatisfy(hit -> {
      assertThat(hit.getFields()).containsEntry("longValue", 21L);
    });
  }

  @Test
  void shouldSupportInstantRangeQueries() throws IOException {
    Instant now = Instant.ofEpochMilli(Instant.now().toEpochMilli());
    try (IndexWriter writer = writer()) {
      writer.addDocument(typesDoc(42, 21L, false, now));
    }
    long before = now.minus(1, ChronoUnit.MINUTES).toEpochMilli();
    long after = now.plus(1, ChronoUnit.MINUTES).toEpochMilli();

    String queryString = String.format("instantValue:[%d TO %d]", before, after);

    QueryResult result = query(Types.class, queryString);
    assertThat(result.getTotalHits()).isOne();
    assertThat(result.getHits()).allSatisfy(hit -> {
      assertThat(hit.getFields()).containsEntry("instantValue", now);
    });
  }

  @Test
  void shouldSupportQueryForBooleanFields() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(typesDoc(21, 42L, true, Instant.now()));
    }

    QueryResult result = query(Types.class, "boolValue:true");
    assertThat(result.getTotalHits()).isOne();
    assertThat(result.getHits()).allSatisfy(hit -> {
      assertThat(hit.getFields()).containsEntry("boolValue", Boolean.TRUE);
    });
  }

  @Test
  void shouldBeAbleToMarshalQueryResultToJson() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(inetOrgPersonDoc("Arthur", "Dent", "Arthur Dent", "4211"));
    }

    QueryResult result = query(InetOrgPerson.class, "Arthur");
    ObjectMapper mapper = new ObjectMapper();

    JsonNode root = mapper.valueToTree(result);
    assertThat(root.get("totalHits").asInt()).isOne();

    JsonNode hit = root.get("hits").get(0);
    assertThat(hit.get("score").asDouble()).isGreaterThan(0d);

    JsonNode fields = hit.get("fields");
    assertThat(fields.get("firstName").asText()).isEqualTo("Arthur");
    assertThat(fields.get("lastName").asText()).isEqualTo("Dent");
    assertThat(fields.get("_type")).isNull();
  }

  @Test
  void shouldBeAbleToMarshalDifferentTypesOfQueryResultToJson() throws IOException {
    Instant now = Instant.ofEpochMilli(Instant.now().toEpochMilli());
    try (IndexWriter writer = writer()) {
      writer.addDocument(typesDoc(21, 42L, true, now));
    }

    QueryResult result = query(Types.class, "intValue:21");
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.registerModule(new JavaTimeModule());

    JsonNode root = mapper.valueToTree(result);
    JsonNode fields = root.get("hits").get(0).get("fields");
    assertThat(fields.get("intValue").asInt()).isEqualTo(21);
    assertThat(fields.get("longValue").asLong()).isEqualTo(42L);
    assertThat(fields.get("boolValue").asBoolean()).isTrue();
    assertThat(fields.get("instantValue").asText()).isEqualTo(now.toString());
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
    document.add(new StringField(FieldNames.TYPE, Simple.class.getName(), Field.Store.YES));
    return document;
  }

  private Document permissionDoc(String content, String permission) {
    Document document = new Document();
    document.add(new TextField("content", content, Field.Store.YES));
    document.add(new StringField(FieldNames.TYPE, Simple.class.getName(), Field.Store.YES));
    document.add(new StringField(FieldNames.PERMISSION, permission, Field.Store.YES));
    return document;
  }

  private Document repositoryDoc(String content, String repository) {
    Document document = new Document();
    document.add(new TextField("content", content, Field.Store.YES));
    document.add(new StringField(FieldNames.TYPE, Simple.class.getName(), Field.Store.YES));
    document.add(new StringField(FieldNames.REPOSITORY, repository, Field.Store.YES));
    return document;
  }

  private Document inetOrgPersonDoc(String firstName, String lastName, String displayName, String carLicense) {
    Document document = new Document();
    document.add(new TextField("firstName", firstName, Field.Store.YES));
    document.add(new TextField("lastName", lastName, Field.Store.YES));
    document.add(new TextField("displayName", displayName, Field.Store.YES));
    document.add(new TextField("carLicense", carLicense, Field.Store.YES));
    document.add(new StringField(FieldNames.TYPE, InetOrgPerson.class.getName(), Field.Store.YES));
    return document;
  }

  private Document personDoc(String lastName) {
    Document document = new Document();
    document.add(new TextField("lastName", lastName, Field.Store.YES));
    document.add(new StringField(FieldNames.TYPE, Person.class.getName(), Field.Store.YES));
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
    document.add(new StringField(FieldNames.TYPE, Types.class.getName(), Field.Store.YES));
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

  static class Simple {
    @Indexed
    private String content;
  }

}
