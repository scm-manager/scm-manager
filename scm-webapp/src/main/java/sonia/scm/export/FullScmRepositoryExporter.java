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
import sonia.scm.ContextEntry;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryExportException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FullScmRepositoryExporter {

  private final EnvironmentInformationXmlGenerator generator;
  private final RepositoryServiceFactory serviceFactory;
  private final RepositoryStoreExporter storeExporter;

  @Inject
  public FullScmRepositoryExporter(EnvironmentInformationXmlGenerator generator, RepositoryServiceFactory serviceFactory, RepositoryStoreExporter storeExporter) {
    this.generator = generator;
    this.serviceFactory = serviceFactory;
    this.storeExporter = storeExporter;
  }

  public void export(Repository repository, OutputStream outputStream) {
    try (
      RepositoryService service = serviceFactory.create(repository);
      BufferedOutputStream bos = new BufferedOutputStream(outputStream);
      GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
      TarArchiveOutputStream taos = new TarArchiveOutputStream(gzos);
    ) {
      writeEnvironmentData(taos);
      writeRepository(service, taos);
      writeStoreData(repository, taos);
      taos.finish();
    } catch (IOException e) {
      throw new RepositoryExportException(
        ContextEntry.ContextBuilder.entity(repository).build(),
        "Could not export repository with metadata",
        e
      );
    }
  }

  private void writeRepository(RepositoryService service, TarArchiveOutputStream taos) throws IOException {
    ByteArrayOutputStream repoBaos = new ByteArrayOutputStream();
    service.getBundleCommand().bundle(repoBaos);
    TarArchiveEntry entry = new TarArchiveEntry(service.getRepository().getName() + ".dump");
    entry.setSize(repoBaos.size());
    taos.putArchiveEntry(entry);
    taos.write(repoBaos.toByteArray());
    taos.closeArchiveEntry();
  }

  private void writeEnvironmentData(TarArchiveOutputStream taos) throws IOException {
    ByteArrayOutputStream envInfoBaos = generator.generate();
    TarArchiveEntry entry = new TarArchiveEntry("scm-environment.xml");
    entry.setSize(envInfoBaos.size());
    taos.putArchiveEntry(entry);
    taos.write(envInfoBaos.toByteArray());
    taos.closeArchiveEntry();
  }

  private void writeStoreData(Repository repository, TarArchiveOutputStream taos) throws IOException {
    File metadata = Files.createFile(Paths.get("metadata")).toFile();
    FileOutputStream metadataFos = new FileOutputStream(metadata);
    storeExporter.export(repository, metadataFos);
    TarArchiveEntry entry = new TarArchiveEntry("scm-metadata.tar");
    entry.setSize(metadata.length());
    taos.putArchiveEntry(entry);
    Files.copy(metadata.toPath(), taos);
    taos.closeArchiveEntry();
    Files.delete(metadata.toPath());
  }
}
