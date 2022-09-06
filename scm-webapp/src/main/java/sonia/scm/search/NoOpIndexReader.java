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
