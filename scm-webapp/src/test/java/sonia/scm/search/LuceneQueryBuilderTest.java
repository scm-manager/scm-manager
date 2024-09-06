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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

@SuppressWarnings("java:S5976")
@SubjectAware(value = "trillian", permissions = "abc")
@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class LuceneQueryBuilderTest {

  private Directory directory;

  @Mock
  private IndexManager opener;

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
  void shouldReturnCount() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(inetOrgPersonDoc("Arthur", "Dent", "Arthur Dent", "4211"));
    }

    long hits = count(InetOrgPerson.class, "Arthur");
    assertThat(hits).isOne();
  }

  @Test
  void shouldMatchPartial() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(personDoc("Trillian"));
    }

    QueryResult result = query(Person.class, "Trill");
    assertThat(result.getTotalHits()).isOne();
  }

  @Test
  void shouldMatchWithHyphen() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(personDoc("Trillian-McMillan"));
    }

    QueryResult result = query(Person.class, "Trillian-McMi");
    assertThat(result.getTotalHits()).isOne();
  }

  @Test
  void shouldMatchQueryWithMultipleFieldsAndHyphen() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(inetOrgPersonDoc("Trillian", "McMillan", "Trillian McMillan", "abc"));
    }

    QueryResult result = query(InetOrgPerson.class, "Trillian-McMi");
    assertThat(result.getTotalHits()).isOne();
  }


  @Test
  void shouldMatchExpertQueryWithHyphen() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(personDoc("Trillian-McMillan"));
    }

    QueryResult result = query(Person.class, "lastName:Trillian-McMi");
    assertThat(result.getTotalHits()).isOne();
  }

  @Test
  @SuppressWarnings("java:S5976")
  void shouldNotAppendWildcardIfStarIsUsed() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(simpleDoc("Trillian"));
    }

    QueryResult result = query(Simple.class, "Tr*ll");
    assertThat(result.getTotalHits()).isZero();
  }

  @Test
  @SuppressWarnings("java:S5976")
  void shouldNotAppendWildcardIfQuestionMarkIsUsed() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(simpleDoc("Trillian"));
    }

    QueryResult result = query(Simple.class, "Tr?ll");
    assertThat(result.getTotalHits()).isZero();
  }

  @Test
  @SuppressWarnings("java:S5976")
  void shouldNotAppendWildcardIfExpertQueryIsUsed() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(simpleDoc("Trillian"));
    }

    QueryResult result = query(Simple.class, "lastName:Trill");
    assertThat(result.getTotalHits()).isZero();
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
  void shouldThrowQueryParseExceptionOnInvalidQuery() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(personDoc("Dent"));
    }
    assertThrows(QueryParseException.class, () -> query(InetOrgPerson.class, ":~:~"));
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
    assertValueField(arthur, "firstName", "Arthur");

    Hit fake = hits.get(1);
    assertValueField(fake, "firstName", "Fake");

    assertThat(arthur.getScore()).isGreaterThan(fake.getScore());
  }

  private void assertValueField(Hit hit, String fieldName, Object value) {
    assertThat(hit.getFields().get(fieldName))
      .isInstanceOfSatisfying(Hit.ValueField.class, field -> {
        assertThat(field.isHighlighted()).isFalse();
        assertThat(field.getValue()).isEqualTo(value);
      });
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
      assertValueField(hit, "content", "Awesome content one");
      assertThat(hit.getScore()).isGreaterThan(0f);
    });
  }

  @Test
  void shouldCountOnlyPermittedHits() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(permissionDoc("Awesome content one", "abc"));
      writer.addDocument(permissionDoc("Awesome content two", "cde"));
      writer.addDocument(permissionDoc("Awesome content three", "fgh"));
    }

    long hits = count(Simple.class, "content:awesome");
    assertThat(hits).isOne();
  }

  @Test
  void shouldFilterByRepository() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(repositoryDoc("Awesome content one", "abc"));
      writer.addDocument(repositoryDoc("Awesome content two", "cde"));
      writer.addDocument(repositoryDoc("Awesome content three", "fgh"));
    }

    QueryResult result = query(
      Simple.class, "content:awesome", builder -> builder.filter(Repository.class, "cde")
    );

    assertThat(result.getTotalHits()).isOne();

    List<Hit> hits = result.getHits();
    assertThat(hits).hasSize(1).allSatisfy(hit -> {
      assertValueField(hit, "content", "Awesome content two");
      assertThat(hit.getScore()).isGreaterThan(0f);
    });
  }

  @Test
  void shouldFilterByIdParts() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(idPartsDoc("Awesome content one", "abc", "one"));
      writer.addDocument(idPartsDoc("Awesome content two", "abc", "two"));
      writer.addDocument(idPartsDoc("Awesome content three", "cde", "two"));
    }

    QueryResult result = query(
      Simple.class, "content:awesome",
      builder -> builder.filter(Repository.class, "abc").filter(String.class, "two")
    );

    List<Hit> hits = result.getHits();
    assertThat(hits).hasSize(1)
      .allSatisfy(hit -> assertValueField(hit, "content", "Awesome content two"));
  }

  @Test
  void shouldReturnStringFields() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(simpleDoc("Awesome"));
    }

    QueryResult result = query(Simple.class, "content:awesome");
    assertThat(result.getTotalHits()).isOne();
    assertThat(result.getHits()).allSatisfy(
      hit -> assertValueField(hit, "content", "Awesome")
    );
  }

  @Test
  void shouldReturnIdOfHit() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(inetOrgPersonDoc("Slarti", "Bartfass", "Slartibartfass", "-"));
    }

    QueryResult result = query(InetOrgPerson.class, "lastName:Bartfass");
    assertThat(result.getTotalHits()).isOne();
    assertThat(result.getHits()).allSatisfy(
      hit -> assertThat(hit.getId()).isEqualTo("Bartfass")
    );
  }

  @Test
  void shouldReturnTypeOfHits() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(simpleDoc("We need the type"));
    }

    QueryResult result = query(Simple.class, "content:type");
    assertThat(result.getType()).isEqualTo(Simple.class);
  }

  @Test
  void shouldSupportIntRangeQueries() throws IOException {
    Instant now = Instant.now();
    try (IndexWriter writer = writer()) {
      writer.addDocument(typesDoc(42, 21L, false, now));
    }

    QueryResult result = query(Types.class, "intValue:[0 TO 100]");
    assertThat(result.getTotalHits()).isOne();
    assertThat(result.getHits()).allSatisfy(
      hit -> assertValueField(hit, "intValue", 42)
    );
  }

  @Test
  void shouldSupportLongRangeQueries() throws IOException {
    Instant now = Instant.now();
    try (IndexWriter writer = writer()) {
      writer.addDocument(typesDoc(42, 21L, false, now));
    }

    QueryResult result = query(Types.class, "longValue:[0 TO 100]");
    assertThat(result.getTotalHits()).isOne();
    assertThat(result.getHits()).allSatisfy(
      hit -> assertValueField(hit, "longValue", 21L)
    );
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
    assertThat(result.getHits()).allSatisfy(
      hit -> assertValueField(hit, "instantValue", now)
    );
  }

  @Test
  void shouldSupportQueryForBooleanFields() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(typesDoc(21, 42L, true, Instant.now()));
    }

    QueryResult result = query(Types.class, "boolValue:true");
    assertThat(result.getTotalHits()).isOne();
    assertThat(result.getHits()).allSatisfy(
      hit -> assertValueField(hit, "boolValue", Boolean.TRUE)
    );
  }

  @Test
  void shouldReturnValueFieldForHighlightedFieldWithoutFragment() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(inetOrgPersonDoc("Marvin", "HoG", "Paranoid Android", "4211"));
    }

    QueryResult result = query(InetOrgPerson.class, "Marvin");
    Hit hit = result.getHits().get(0);
    assertValueField(hit, "displayName", "Paranoid Android");
  }

  @Test
  void shouldLimitHitsByDefaultSize() throws IOException {
    try (IndexWriter writer = writer()) {
      for (int i = 0; i < 20; i++)
        writer.addDocument(simpleDoc("counter " + i));
    }

    QueryResult result = query(Simple.class, "content:counter");
    assertThat(result.getTotalHits()).isEqualTo(20L);
    assertThat(result.getHits()).hasSize(10);
  }

  @Test
  void shouldLimitHitsByConfiguredSize() throws IOException {
    try (IndexWriter writer = writer()) {
      for (int i = 0; i < 20; i++)
        writer.addDocument(simpleDoc("counter " + (i + 1)));
    }

    QueryResult result = query(Simple.class, "content:counter", null, 2);
    assertThat(result.getTotalHits()).isEqualTo(20L);
    assertThat(result.getHits()).hasSize(2);

    assertContainsValues(
      result, "content", "counter 1", "counter 2"
    );
  }

  @Test
  void shouldRespectStartValue() throws IOException {
    try (IndexWriter writer = writer()) {
      for (int i = 0; i < 20; i++)
        writer.addDocument(simpleDoc("counter " + (i + 1)));
    }

    QueryResult result = query(Simple.class, "content:counter", 10, 3);
    assertThat(result.getTotalHits()).isEqualTo(20L);
    assertThat(result.getHits()).hasSize(3);

    assertContainsValues(
      result, "content", "counter 11", "counter 12", "counter 13"
    );
  }

  private void assertContainsValues(QueryResult result, String fieldName, Object... expectedValues) {
    List<Object> values = result.getHits().stream().map(hit -> {
      Hit.ValueField content = (Hit.ValueField) hit.getFields().get(fieldName);
      return content.getValue();
    }).toList();
    assertThat(values).containsExactly(expectedValues);
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
    JsonNode firstName = fields.get("firstName");
    assertThat(firstName.get("highlighted").asBoolean()).isFalse();
    assertThat(firstName.get("value").asText()).isEqualTo("Arthur");

    JsonNode displayName = fields.get("displayName");
    assertThat(displayName.get("highlighted").asBoolean()).isTrue();
    assertThat(displayName.get("fragments").get(0).asText()).contains("<|[[--Arthur--]]|>");
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
    assertThat(fields.get("intValue").get("value").asInt()).isEqualTo(21);
    assertThat(fields.get("longValue").get("value").asLong()).isEqualTo(42L);
    assertThat(fields.get("boolValue").get("value").asBoolean()).isTrue();
    assertThat(fields.get("instantValue").get("value").asText()).isEqualTo(now.toString());
  }

  @Test
  void shouldReturnEmptyRepository() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(simpleDoc("Trillian"));
    }

    QueryResult result = query(Simple.class, "Trillian");
    Hit hit = result.getHits().get(0);
    assertThat(hit.getRepositoryId()).isEmpty();
  }

  @Test
  void shouldReturnRepositoryAsPartOfHit() throws IOException {
    try (IndexWriter writer = writer()) {
      Document document = simpleDoc("Trillian");
      document.add(new StoredField(FieldNames.REPOSITORY, "4211"));
      writer.addDocument(document);
    }

    QueryResult result = query(Simple.class, "Trillian");
    Hit hit = result.getHits().get(0);
    assertThat(hit.getRepositoryId()).contains("4211");
  }

  @Test
  void shouldQueryByEnumField() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(animalPerson("Trillian", Animal.PENGUIN));
    }

    QueryResult result = query(PersonWithAnimal.class, "animal:penguin");
    assertThat(result.getTotalHits()).isOne();
  }

  @Test
  void shouldQueryByEnumFieldAndIgnoreCase() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(animalPerson("Arthur", Animal.ALPACA));
    }

    QueryResult result = query(PersonWithAnimal.class, "animal:AlPaCa");
    assertThat(result.getTotalHits()).isOne();
  }

  @Test
  void shouldFindNothingBecauseTypesHasNoDefaultFields() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(typesDoc(1, 2, true, Instant.now()));
    }

    QueryResult result = query(Types.class, "1 OR 2 OR true");
    assertThat(result.getTotalHits()).isZero();
  }

  @Test
  void shouldQueryWithWildcardsForEveryTermBecauseQueryContainsNoOperators() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(personDoc("Holmes"));
      writer.addDocument(personDoc("Watson"));
      writer.addDocument(personDoc("Moriarty"));
    }

    QueryResult result = query(Person.class, "hol wat mor");
    assertThat(result.getTotalHits()).isEqualTo(3);
  }

  @ParameterizedTest
  @CsvSource({
    //Wildcard Modifiers
    "hol* wat mori,1,Holmes",
    "hol?es wat mori,1,Holmes",
    //Boolean Operators
    "holmes OR wat,1,Holmes",
    "holmes AND hol,0,",
    "watson NOT wat,1,Watson",
    "watson !wat,1,Watson",
    "watson -wat,1,Watson",
    "watson +wat,0,",
    //Ranges
    "[A TO Z],3,",
    "{Holmes TO Watson},1,Moriarty",
    //Boosting
    "holmes^2,1,Holmes",
    //Grouping
    "(hol),0,",
    //Escaping
    "\\hol watson,1,Watson",
    //Field name
    "lastName:hol,0,",
    //PhrasesA
    "\"Hol\",0,"
  })
  void shouldQueryWithoutImplicitlyAddedWildCardsBecauseOperatorsAreUsed(String queryString, String expectedResultCount, String expectedValue) throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(personDoc("Holmes"));
      writer.addDocument(personDoc("Watson"));
      writer.addDocument(personDoc("Moriarty"));
    }

    QueryResult result = query(Person.class, queryString);
    assertThat(result.getTotalHits()).isEqualTo(Integer.parseInt(expectedResultCount));

    if (expectedValue != null) {
      assertContainsValues(result, "lastName", expectedValue);
    }
  }

  @ParameterizedTest
  @CsvSource({"holmes watson moriarty,SIMPLE", "holmes OR watson OR moriarty,EXPERT"})
  void shouldReturnQueryResultWithCorrectType(String queryString, String expectedQueryType) throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(personDoc("Holmes"));
      writer.addDocument(personDoc("Watson"));
      writer.addDocument(personDoc("Moriarty"));
    }

    QueryResult result = query(Person.class, queryString);
    assertThat(result.getQueryType().name()).isEqualTo(expectedQueryType);
  }

  @ParameterizedTest
  @CsvSource({"holmes watson moriarty,SIMPLE", "holmes OR watson OR moriarty,EXPERT"})
  void shouldReturnQueryCountResultWithCorrectType(String queryString, String expectedQueryType) throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(personDoc("Holmes"));
      writer.addDocument(personDoc("Watson"));
      writer.addDocument(personDoc("Moriarty"));
    }

    QueryCountResult result = countQuery(Person.class, queryString);
    assertThat(result.getQueryType().name()).isEqualTo(expectedQueryType);
  }

  @Test
  void shouldQueryExactName() throws IOException {
    try (IndexWriter writer = writer()) {
      writer.addDocument(personDoc("Sherlock Holmes"));
      writer.addDocument(personDoc("Holmes Sherlock"));
    }

    QueryResult result = query(Person.class, "\"Sherlock Holmes\"");
    assertThat(result.getQueryType()).isEqualTo(QueryType.EXPERT);
    assertContainsValues(result, "lastName", "Sherlock Holmes");
  }

  private QueryResult query(Class<?> type, String queryString) throws IOException {
    return query(type, queryString, null, null);
  }

  private <T> long count(Class<T> type, String queryString) throws IOException {
    return countQuery(type, queryString).getTotalHits();
  }

  private <T> QueryCountResult countQuery(Class<T> type, String queryString) throws IOException {
    try (DirectoryReader reader = DirectoryReader.open(directory)) {
      SearchableTypeResolver resolver = new SearchableTypeResolver(type);
      LuceneSearchableType searchableType = resolver.resolve(type);
      lenient().when(opener.openForRead(searchableType, "default")).thenReturn(reader);
      LuceneQueryBuilder<T> builder = new LuceneQueryBuilder<T>(
        opener, "default", searchableType, new AnalyzerFactory().create(searchableType)
      );
      return builder.count(queryString);
    }
  }

  private <T> QueryResult query(Class<?> type, String queryString, Integer start, Integer limit) throws IOException {
    return query(type, queryString, builder -> {
      if (start != null) {
        builder.start(start);
      }
      if (limit != null) {
        builder.limit(limit);
      }
    });
  }

  private <T> QueryResult query(Class<T> type, String queryString, Consumer<LuceneQueryBuilder<T>> consumer) throws IOException {
    try (DirectoryReader reader = DirectoryReader.open(directory)) {
      SearchableTypeResolver resolver = new SearchableTypeResolver(type);
      LuceneSearchableType searchableType = resolver.resolve(type);

      lenient().when(opener.openForRead(searchableType, "default")).thenReturn(reader);
      LuceneQueryBuilder<T> builder = new LuceneQueryBuilder<>(
        opener, "default", searchableType, new AnalyzerFactory().create(searchableType)
      );
      consumer.accept(builder);
      return builder.execute(queryString);
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
    return document;
  }

  private Document permissionDoc(String content, String permission) {
    Document document = new Document();
    document.add(new TextField("content", content, Field.Store.YES));
    document.add(new StringField(FieldNames.PERMISSION, permission, Field.Store.YES));
    return document;
  }

  private Document repositoryDoc(String content, String repository) {
    Document document = new Document();
    document.add(new TextField("content", content, Field.Store.YES));
    document.add(new StringField(FieldNames.REPOSITORY, repository, Field.Store.YES));
    return document;
  }

  private Document idPartsDoc(String content, String repository, String other) {
    Document document = new Document();
    document.add(new TextField("content", content, Field.Store.YES));
    document.add(new StringField(FieldNames.REPOSITORY, repository, Field.Store.YES));
    document.add(new StringField("_string", other, Field.Store.YES));
    return document;
  }

  private Document inetOrgPersonDoc(String firstName, String lastName, String displayName, String carLicense) {
    Document document = new Document();
    document.add(new TextField("firstName", firstName, Field.Store.YES));
    document.add(new TextField("lastName", lastName, Field.Store.YES));
    document.add(new TextField("displayName", displayName, Field.Store.YES));
    document.add(new TextField("carLicense", carLicense, Field.Store.YES));
    document.add(new StringField(FieldNames.ID, lastName, Field.Store.YES));
    return document;
  }

  private Document personDoc(String lastName) {
    Document document = new Document();
    document.add(new TextField("lastName", lastName, Field.Store.YES));
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
    return document;
  }

  private Document animalPerson(String name, Animal animal) {
    Document document = new Document();
    document.add(new TextField("name", name, Field.Store.YES));
    document.add(new StringField("animal", animal.name(), Field.Store.YES));
    return document;
  }

  enum Animal {
    PENGUIN, ALPACA
  }

  @Getter
  @IndexedType
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

  @Getter
  @IndexedType
  static class Person {

    @Indexed(defaultQuery = true)
    private String lastName;
  }

  @Getter
  @IndexedType
  static class InetOrgPerson extends Person {

    @Indexed(defaultQuery = true, boost = 2f)
    private String firstName;

    @Indexed(defaultQuery = true, highlighted = true)
    private String displayName;

    @Indexed
    private String carLicense;
  }

  @Getter
  @IndexedType
  static class Simple {
    @Indexed(defaultQuery = true)
    private String content;
  }

  @Getter
  @IndexedType
  static class PersonWithAnimal {
    @Indexed(defaultQuery = true)
    private String name;

    @Indexed
    private Animal animal;
  }

}
