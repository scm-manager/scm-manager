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

package sonia.scm.export;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FullScmRepositoryExporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(FullScmRepositoryExporter.class);

  private final EnvironmentInformationXmlGenerator generator;
  private final RepositoryServiceFactory serviceFactory;
  private final RepositoryStoreExporter storeExporter;

  @Inject
  public FullScmRepositoryExporter(EnvironmentInformationXmlGenerator generator, RepositoryServiceFactory serviceFactory, RepositoryStoreExporter storeExporter) {
    this.generator = generator;
    this.serviceFactory = serviceFactory;
    this.storeExporter = storeExporter;
  }

  public OutputStream export(Repository repository) {
    try (
      RepositoryService service = serviceFactory.create(repository);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      BufferedOutputStream bos = new BufferedOutputStream(baos);
      GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
      TarArchiveOutputStream taos = new TarArchiveOutputStream(gzos);
    ) {

      writeRepository(service, taos);
      writeStoreData(repository, taos);
      writeEnvironmentData(taos);

      taos.finish();
      return taos;
    } catch (IOException e) {
      LOGGER.error("Could not export repository with metadata: {}", repository, e);
    }
    throw new InternalRepositoryException(
      ContextEntry.ContextBuilder.entity(repository),
      "Could not export repository with metadata"
    );
  }

  private void writeRepository(RepositoryService service, TarArchiveOutputStream taos) throws IOException {
    ByteArrayOutputStream repoBaos = new ByteArrayOutputStream();
    service.getBundleCommand().bundle(repoBaos);
    taos.putArchiveEntry(new TarArchiveEntry("repository"));
    taos.write(repoBaos.toByteArray());
    taos.closeArchiveEntry();
  }

  private void writeEnvironmentData(TarArchiveOutputStream taos) throws IOException {
    ByteArrayOutputStream envInfoBaos = generator.generate();
    taos.putArchiveEntry(new TarArchiveEntry("environment"));
    taos.write(envInfoBaos.toByteArray());
    taos.closeArchiveEntry();
  }

  private void writeStoreData(Repository repository, TarArchiveOutputStream taos) throws IOException {
    ByteArrayOutputStream metaDataBaos = new ByteArrayOutputStream();
    storeExporter.export(repository, metaDataBaos);
    if (metaDataBaos.size() > 0) {
      taos.putArchiveEntry(new TarArchiveEntry("metadata"));
      taos.write(metaDataBaos.toByteArray());
      taos.closeArchiveEntry();
    }
  }

}
