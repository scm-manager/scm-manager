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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static sonia.scm.search.FieldNames.ID;
import static sonia.scm.search.FieldNames.PERMISSION;

class LuceneIndex<T> implements Index<T>, AutoCloseable {

  private static final Logger LOG = LoggerFactory.getLogger(LuceneIndex.class);

  private final IndexDetails details;
  private final LuceneSearchableType searchableType;
  private final SharableIndexWriter writer;

  LuceneIndex(IndexParams params, Supplier<IndexWriter> writerFactory) {
    this.details = params;
    this.searchableType = params.getSearchableType();
    this.writer = new SharableIndexWriter(writerFactory);
    this.open();
  }

  void open() {
    writer.open();
  }

  @VisibleForTesting
  SharableIndexWriter getWriter() {
    return writer;
  }

  @Override
  public IndexDetails getDetails() {
    return details;
  }

  @Override
  public void store(Id<T> id, String permission, T object) {
    Document document = searchableType.getTypeConverter().convert(object);

    String mainId = id.getMainId();
    Map<String,String> others = Ids.others(id);

    try {
      field(document, ID, Ids.id(mainId, others));

      for (Map.Entry<String, String> e : others.entrySet()) {
        field(document, "_" + e.getKey(), e.getValue());
      }

      if (!Strings.isNullOrEmpty(permission)) {
        field(document, PERMISSION, permission);
      }

      writer.updateDocument(idTerm(id), document);
    } catch (IOException e) {
      throw new SearchEngineException("failed to add document to index", e);
    }
  }

  @Nonnull
  private Term idTerm(Id<T> id) {
    String mainId = id.getMainId();
    Map<String,String> others = Ids.others(id);
    return new Term(ID, Ids.id(mainId, others));
  }


  private void field(Document document, String type, String name) {
    document.add(new StringField(type, name, Field.Store.YES));
  }

  @Override
  public Deleter<T> delete() {
    return new LuceneDeleter();
  }

  @Override
  public void close() {
    try {
      writer.close();
    } catch (IOException e) {
      throw new SearchEngineException("failed to close index writer", e);
    }
  }

  private class LuceneDeleter implements Deleter<T> {

    @Override
    public void byId(Id<T> id) {
      try {
        LOG.debug("delete document(s) by id {} from index {}", id, details);
        writer.deleteDocuments(idTerm(id));
      } catch (IOException e) {
        throw new SearchEngineException("failed to delete document from index", e);
      }
    }

    @Override
    public void all() {
      try {
        LOG.debug("deleted all documents from index {}", details);
        writer.deleteAll();
      } catch (IOException ex) {
        throw new SearchEngineException("failed to delete documents by type " + searchableType.getName() + " from index", ex);
      }
    }

    @Override
    public DeleteBy by(Class<?> type, String id) {
      return new LuceneDeleteBy(type, id);
    }
  }

  private class LuceneDeleteBy implements DeleteBy {

    private final Map<Class<?>, String> map = new HashMap<>();

    private LuceneDeleteBy(Class<?> type, String id) {
      map.put(type, id);
    }

    @Override
    public DeleteBy and(Class<?> type, String id) {
      map.put(type, id);
      return this;
    }

    @Override
    public void execute() {
      Query query = Queries.filterQuery(map);
      try {
        LOG.debug("delete document(s) by query {} from index {}", query, details);
        writer.deleteDocuments(query);
      } catch (IOException e) {
        throw new SearchEngineException("failed to delete document from index", e);
      }
    }

  }
}
