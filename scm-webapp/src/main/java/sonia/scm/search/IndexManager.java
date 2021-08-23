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

import lombok.Data;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import sonia.scm.SCMContextProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class IndexManager {

  private final Path directory;
  private final AnalyzerFactory analyzerFactory;
  private final IndexXml indexXml;

  @Inject
  public IndexManager(SCMContextProvider context, AnalyzerFactory analyzerFactory) {
    directory = context.resolve(Paths.get("index"));
    this.analyzerFactory = analyzerFactory;
    this.indexXml = readIndexXml();
  }

  private IndexXml readIndexXml() {
    Path path = directory.resolve("index.xml");
    if (Files.exists(path)) {
      return JAXB.unmarshal(path.toFile(), IndexXml.class);
    }
    return new IndexXml();
  }

  public List<? extends IndexDetails> all() {
    return Collections.unmodifiableList(indexXml.indices);
  }

  public IndexReader openForRead(LuceneSearchableType type, String indexName) throws IOException {
    Path path = resolveIndexDirectory(type, indexName);
    return DirectoryReader.open(FSDirectory.open(path));
  }

  public IndexWriter openForWrite(IndexParams indexParams) throws IOException {
    IndexWriterConfig config = new IndexWriterConfig(analyzerFactory.create(indexParams.getSearchableType(), indexParams.getOptions()));
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

    Path path = resolveIndexDirectory(indexParams);
    if (!Files.exists(path)) {
      indexXml.getIndices().add(new LuceneIndexDetails(indexParams.getType(), indexParams.getIndex()));
      store();
    }

    return new IndexWriter(FSDirectory.open(path), config);
  }

  private Path resolveIndexDirectory(IndexParams indexParams) {
    return directory.resolve(indexParams.getSearchableType().getName()).resolve(indexParams.getIndex());
  }

  private Path resolveIndexDirectory(LuceneSearchableType searchableType, String indexName) {
    return directory.resolve(searchableType.getName()).resolve(indexName);
  }

  private void store() {
    if (!Files.exists(directory)) {
      try {
        Files.createDirectory(directory);
      } catch (IOException e) {
        throw new SearchEngineException("failed to create index directory");
      }
    }
    Path path = directory.resolve("index.xml");
    JAXB.marshal(indexXml, path.toFile());
  }

  @Data
  @XmlRootElement(name = "indices")
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class IndexXml {

    @XmlElement(name = "index")
    private List<LuceneIndexDetails> indices = new ArrayList<>();

  }
}
