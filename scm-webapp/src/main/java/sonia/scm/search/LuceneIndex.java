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

import com.google.common.base.Strings;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import java.io.IOException;

import static sonia.scm.search.FieldNames.ID;
import static sonia.scm.search.FieldNames.PERMISSION;
import static sonia.scm.search.FieldNames.REPOSITORY;
import static sonia.scm.search.FieldNames.TYPE;
import static sonia.scm.search.FieldNames.UID;

public class LuceneIndex implements Index {

  private final SearchableTypeResolver resolver;
  private final IndexWriter writer;

  LuceneIndex(SearchableTypeResolver resolver, IndexWriter writer) {
    this.resolver = resolver;
    this.writer = writer;
  }

  @Override
  public void store(Id id, String permission, Object object) {
    SearchableType type = resolver.resolve(object);
    String uid = createUid(id, type);
    Document document = type.getTypeConverter().convert(object);
    try {
      field(document, UID, uid);
      field(document, ID, id.getValue());
      id.getRepository().ifPresent(repository -> field(document, REPOSITORY, repository));
      field(document, TYPE, type.getName());
      if (!Strings.isNullOrEmpty(permission)) {
        field(document, PERMISSION, permission);
      }
      writer.updateDocument(new Term(UID, uid), document);
    } catch (IOException e) {
      throw new SearchEngineException("failed to add document to index", e);
    }
  }

  private String createUid(Id id, SearchableType type) {
    return id.asString() + "/" + type.getName();
  }

  private void field(Document document, String type, String name) {
    document.add(new StringField(type, name, Field.Store.YES));
  }

  @Override
  public void delete(Id id, Class<?> type) {
    SearchableType searchableType = resolver.resolve(type);
    try {
      writer.deleteDocuments(new Term(UID, createUid(id, searchableType)));
    } catch (IOException e) {
      throw new SearchEngineException("failed to delete document from index", e);
    }
  }

  @Override
  public void deleteByRepository(String repository) {
    try {
      writer.deleteDocuments(new Term(REPOSITORY, repository));
    } catch (IOException ex) {
      throw new SearchEngineException("failed to delete documents by repository " + repository + " from index", ex);
    }
  }

  @Override
  public void deleteByType(Class<?> type) {
    SearchableType searchableType = resolver.resolve(type);
    deleteByTypeName(searchableType.getName());
  }

  @Override
  public void deleteByTypeName(String typeName) {
    try {
      writer.deleteDocuments(new Term(TYPE, typeName));
    } catch (IOException ex) {
      throw new SearchEngineException("failed to delete documents by repository " + typeName + " from index", ex);
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
