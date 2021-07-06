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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LuceneQueryResult implements QueryResult {

  @JsonProperty
  private final long totalHits;
  @JsonProperty
  private final List<Hit> hits;

  private LuceneQueryResult(long totalHits, List<Hit> hits) {
    this.totalHits = totalHits;
    this.hits = hits;
  }

  static LuceneQueryResult of(IndexSearcher searcher, TopDocs topDocs) throws IOException {
    List<Hit> hits = new ArrayList<>();
    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
      Document document = searcher.doc(scoreDoc.doc);
      hits.add(new LuceneHit(document, scoreDoc.score));
    }
    return new LuceneQueryResult(topDocs.totalHits.value, hits);
  }

  @Override
  public long totalHits() {
    return totalHits;
  }

  @Override
  public List<Hit> hits() {
    return hits;
  }

  @JsonSerialize(using = HitSerializer.class)
  public static class LuceneHit implements Hit {

    private final Document document;
    private final float score;

    public LuceneHit(Document document, float score) {
      this.document = document;
      this.score = score;
    }

    @Override
    public Optional<String> get(String name) {
      return Optional.ofNullable(document.get(name));
    }

    @Override
    public Optional<Integer> getInteger(String name) {
      IndexableField field = document.getField(name);
      if (field != null) {
        return Optional.of(field.numericValue().intValue());
      }
      return Optional.empty();
    }

    @Override
    public Optional<Long> getLong(String name) {
      IndexableField field = document.getField(name);
      if (field != null) {
        return Optional.of(field.numericValue().longValue());
      }
      return Optional.empty();
    }

    @Override
    public Optional<Boolean> getBoolean(String name) {
      IndexableField field = document.getField(name);
      if (field != null) {
        return Optional.of(Boolean.parseBoolean(field.stringValue()));
      }
      return Optional.empty();
    }

    @Override
    public Optional<Instant> getInstant(String name) {
      return getLong(name).map(Instant::ofEpochMilli);
    }

    @Override
    public float getScore() {
      return score;
    }
  }

  public static class HitSerializer extends StdSerializer<LuceneHit> {
    public HitSerializer() {
      this(null);
    }

    public HitSerializer(Class<LuceneHit> t) {
      super(t);
    }

    @Override
    public void serialize(LuceneHit doc, JsonGenerator gen, SerializerProvider provider) throws IOException {
      gen.writeStartObject();
      gen.writeNumberField("score", doc.score);
      gen.writeObjectFieldStart("fields");

      for (IndexableField field : doc.document) {
        String name = field.name();
        if (!name.startsWith("_")) {
          // TODO support numeric, date and boolean fields
          gen.writeStringField(field.name(), field.stringValue());
        }
      }

      gen.writeEndObject();
      gen.writeEndObject();
    }
  }

}
