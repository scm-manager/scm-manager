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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.function.Supplier;

import static sonia.scm.search.FieldNames.ID;
import static sonia.scm.search.FieldNames.PERMISSION;
import static sonia.scm.search.FieldNames.REPOSITORY;

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
  public void store(Id id, String permission, Object object) {
    Document document = searchableType.getTypeConverter().convert(object);
    try {
      field(document, ID, id.asString());
      id.getRepository().ifPresent(repository -> field(document, REPOSITORY, repository));
      if (!Strings.isNullOrEmpty(permission)) {
        field(document, PERMISSION, permission);
      }
      writer.updateDocument(idTerm(id), document);
    } catch (IOException e) {
      throw new SearchEngineException("failed to add document to index", e);
    }
  }

  @Nonnull
  private Term idTerm(Id id) {
    return new Term(ID, id.asString());
  }

  private void field(Document document, String type, String name) {
    document.add(new StringField(type, name, Field.Store.YES));
  }

  @Override
  public Deleter delete() {
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

  private class LuceneDeleter implements Deleter {

    @Override
    public void byId(Id id) {
      try {
        long count = writer.deleteDocuments(idTerm(id));
        LOG.debug("delete {} document by id {}", count, id);
      } catch (IOException e) {
        throw new SearchEngineException("failed to delete document from index", e);
      }
    }

    @Override
    public void all() {
      try {
        long count = writer.deleteAll();
        LOG.debug("deleted all {} documents", count);
      } catch (IOException ex) {
        throw new SearchEngineException("failed to delete documents by type " + searchableType.getName() + " from index", ex);
      }
    }

    @Override
    public void byRepository(String repositoryId) {
      try {
        long count = writer.deleteDocuments(repositoryTerm(repositoryId));
        LOG.debug("deleted {} documents by repository {}", count, repositoryId);
      } catch (IOException ex) {
        throw new SearchEngineException("failed to delete documents by repository " + repositoryId + " from index", ex);
      }
    }

    @Nonnull
    private Term repositoryTerm(String repositoryId) {
      return new Term(REPOSITORY, repositoryId);
    }
  }
}
