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
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import java.io.IOException;


public class LuceneIndex implements Index {

  @VisibleForTesting
  static final String FIELD_UID = "_uid";

  @VisibleForTesting
  static final String FIELD_ID = "_id";

  @VisibleForTesting
  static final String FIELD_TYPE = "_type";

  @VisibleForTesting
  static final String FIELD_REPOSITORY = "_repository";

  private final DocumentConverter converter;
  private final IndexWriter writer;

  LuceneIndex(DocumentConverter converter, IndexWriter writer) {
    this.converter = converter;
    this.writer = writer;
  }

  @Override
  public void store(Id id, Object object) {
    String uid = createUid(id, object.getClass());
    Document document = converter.convert(object);
    try {
      field(document, FIELD_UID, uid);
      field(document, FIELD_ID, id.getValue());
      id.getRepository().ifPresent(repository -> field(document, FIELD_REPOSITORY, repository));
      field(document, FIELD_TYPE, object.getClass().getName());
      writer.updateDocument(new Term(FIELD_UID, uid), document);
    } catch (IOException e) {
      throw new SearchEngineException("failed to add document to index");
    }
  }

  private String createUid(Id id, Class<?> type) {
    return id.asString() + "/" + type.getName();
  }

  private void field(Document document, String type, String name) {
    document.add(new StringField(type, name, Field.Store.YES));
  }

  @Override
  public void delete(Id id, Class<?> type) {
    try {
      writer.deleteDocuments(new Term(FIELD_UID, createUid(id, type)));
    } catch (IOException e) {
      throw new SearchEngineException("failed to delete document from index");
    }
  }

  @Override
  public void deleteByRepository(String repository) {
    try {
      writer.deleteDocuments(new Term(FIELD_REPOSITORY, repository));
    } catch (IOException ex) {
      throw new SearchEngineException("failed to delete documents by repository " + repository + " from index");
    }
  }

  @Override
  public void deleteByType(Class<?> type) {
    try {
      writer.deleteDocuments(new Term(FIELD_TYPE, type.getName()));
    } catch (IOException ex) {
      throw new SearchEngineException("failed to delete documents by repository " + type + " from index");
    }
  }

  @Override
  public void close() {
    try {
      writer.close();
    } catch (IOException e) {
      throw new SearchEngineException("failed to close index writer", e);
    }
  }
}
