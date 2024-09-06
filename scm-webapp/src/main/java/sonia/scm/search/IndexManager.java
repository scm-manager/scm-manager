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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.PluginLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class IndexManager {

  private final Path directory;
  private final AnalyzerFactory analyzerFactory;
  private final IndexXml indexXml;

  @Inject
  public IndexManager(SCMContextProvider context, PluginLoader pluginLoader, AnalyzerFactory analyzerFactory) {
    directory = context.resolve(Paths.get("index"));
    this.analyzerFactory = analyzerFactory;
    this.indexXml = readIndexXml(pluginLoader.getUberClassLoader());
  }

  private IndexXml readIndexXml(ClassLoader uberClassLoader) {
    Path path = directory.resolve("index.xml");
    if (Files.exists(path)) {
      ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
      try {
        Thread.currentThread().setContextClassLoader(uberClassLoader);
        return JAXB.unmarshal(path.toFile(), IndexXml.class);
      } finally {
        Thread.currentThread().setContextClassLoader(contextClassLoader);
      }
    }
    return new IndexXml();
  }

  public Collection<? extends IndexDetails> all() {
    return Collections.unmodifiableSet(indexXml.indices);
  }

  public IndexReader openForRead(LuceneSearchableType type, String indexName) throws IOException {
    Path path = resolveIndexDirectory(type, indexName);
    FSDirectory fsDirectory = FSDirectory.open(path);
    if (DirectoryReader.indexExists(fsDirectory)) {
      return DirectoryReader.open(fsDirectory);
    }
    return new NoOpIndexReader();
  }

  public IndexWriter openForWrite(IndexParams indexParams) {
    IndexWriterConfig config = new IndexWriterConfig(analyzerFactory.create(indexParams.getSearchableType()));
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

    Path path = resolveIndexDirectory(indexParams);
    if (!Files.exists(path)) {
      store(new LuceneIndexDetails(indexParams.getType(), indexParams.getIndex()));
    }

    try {
      return new IndexWriter(FSDirectory.open(path), config);
    } catch (IOException ex) {
      throw new SearchEngineException("failed to open index at " + path, ex);
    }
  }

  private Path resolveIndexDirectory(IndexParams indexParams) {
    return directory.resolve(indexParams.getSearchableType().getName()).resolve(indexParams.getIndex());
  }

  private Path resolveIndexDirectory(LuceneSearchableType searchableType, String indexName) {
    return directory.resolve(searchableType.getName()).resolve(indexName);
  }

  private synchronized void store(LuceneIndexDetails details) {
    if (!indexXml.getIndices().add(details)) {
      return;
    }

    if (!Files.exists(directory)) {
      try {
        Files.createDirectory(directory);
      } catch (IOException e) {
        throw new SearchEngineException("failed to create index directory", e);
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
    private Set<LuceneIndexDetails> indices = new HashSet<>();

  }
}
