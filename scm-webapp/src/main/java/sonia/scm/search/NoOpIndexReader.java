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

import org.apache.lucene.index.CompositeReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StoredFieldVisitor;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("java:S2272") // Exception obsolete
public class NoOpIndexReader extends CompositeReader {

  public NoOpIndexReader() {
    super();
  }

  @Override
  protected List<? extends IndexReader> getSequentialSubReaders() {
    return Collections.emptyList();
  }

  @Override
  public Fields getTermVectors(int docID) {
    return new Fields() {
      @Override
      public Iterator<String> iterator() {
        return new Iterator<>() {
          @Override
          public boolean hasNext() {
            return false;
          }

          @Override
          public String next() {
            return "";
          }
        };
      }

      @Override
      public Terms terms(String field) {
        return new Terms() {
          @Override
          public TermsEnum iterator() {
            return null;
          }

          @Override
          public long size() {
            return 0;
          }

          @Override
          public long getSumTotalTermFreq() {
            return 0;
          }

          @Override
          public long getSumDocFreq() {
            return 0;
          }

          @Override
          public int getDocCount() {
            return 0;
          }

          @Override
          public boolean hasFreqs() {
            return false;
          }

          @Override
          public boolean hasOffsets() {
            return false;
          }

          @Override
          public boolean hasPositions() {
            return false;
          }

          @Override
          public boolean hasPayloads() {
            return false;
          }
        };
      }

      @Override
      public int size() {
        return 0;
      }
    };
  }

  @Override
  public int numDocs() {
    return 0;
  }

  @Override
  public int maxDoc() {
    return 0;
  }

  @Override
  public void document(int docID, StoredFieldVisitor visitor) throws IOException {
    // do nothing
  }

  @Override
  protected void doClose() {
    // do nothing
  }

  @Override
  public IndexReader.CacheHelper getReaderCacheHelper() {
    return null;
  }

  @Override
  public int docFreq(Term term) {
    return 0;
  }

  @Override
  public long totalTermFreq(Term term) {
    return 0;
  }

  @Override
  public long getSumDocFreq(String field) {
    return 0;
  }

  @Override
  public int getDocCount(String field) {
    return 0;
  }

  @Override
  public long getSumTotalTermFreq(String field) {
    return 0;
  }

}
