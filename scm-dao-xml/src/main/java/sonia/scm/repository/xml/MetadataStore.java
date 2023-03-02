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

package sonia.scm.repository.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.store.CopyOnWrite;
import sonia.scm.store.StoreConstants;
import sonia.scm.update.UpdateStepRepositoryMetadataAccess;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.nio.file.Path;

import static sonia.scm.store.CopyOnWrite.compute;

public class MetadataStore implements UpdateStepRepositoryMetadataAccess<Path> {

  private static final Logger LOG = LoggerFactory.getLogger(MetadataStore.class);

  private final JAXBContext jaxbContext;

  public MetadataStore() {
    try {
       jaxbContext = JAXBContext.newInstance(Repository.class);
    } catch (JAXBException ex) {
      throw new IllegalStateException("failed to create jaxb context for repository", ex);
    }
  }

  public Repository read(Path path) {
    LOG.trace("read repository metadata from {}", path);
    return compute(() -> {
      try {
        return (Repository) jaxbContext.createUnmarshaller().unmarshal(resolveDataPath(path).toFile());
      } catch (JAXBException ex) {
        throw new InternalRepositoryException(
          ContextEntry.ContextBuilder.entity(Path.class, path.toString()).build(), "failed read repository metadata", ex
        );
      }
    }).withLockedFileForRead(path);
  }

  void write(Path path, Repository repository) {
    LOG.trace("write repository metadata of {} to {}", repository, path);
    try {
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      CopyOnWrite.withTemporaryFile(
        temp -> marshaller.marshal(repository, temp.toFile()),
        resolveDataPath(path)
      );
    } catch (JAXBException ex) {
      throw new InternalRepositoryException(repository, "failed write repository metadata", ex);
    }
  }

  private Path resolveDataPath(Path repositoryPath) {
    return repositoryPath.resolve(StoreConstants.REPOSITORY_METADATA.concat(StoreConstants.FILE_EXTENSION));
  }
}
