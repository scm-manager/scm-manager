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

package sonia.scm.importexport;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import sonia.scm.ContextEntry;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ExportFailedException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FullScmRepositoryExporter {

  private final EnvironmentInformationXmlGenerator generator;
  private final RepositoryServiceFactory serviceFactory;
  private final TarArchiveRepositoryStoreExporter storeExporter;

  @Inject
  public FullScmRepositoryExporter(EnvironmentInformationXmlGenerator generator,
                                   RepositoryServiceFactory serviceFactory,
                                   TarArchiveRepositoryStoreExporter storeExporter) {
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
      throw new ExportFailedException(
        ContextEntry.ContextBuilder.entity(repository).build(),
        "Could not export repository with metadata",
        e
      );
    }
  }

  private void writeRepository(RepositoryService service, TarArchiveOutputStream taos) throws IOException {
    File repositoryFile = Files.createFile(Paths.get("repository")).toFile();
    try (FileOutputStream repositoryFos = new FileOutputStream(repositoryFile)) {
      service.getBundleCommand().bundle(repositoryFos);
      TarArchiveEntry entry = new TarArchiveEntry(service.getRepository().getName() + ".dump");
      entry.setSize(repositoryFile.length());
      taos.putArchiveEntry(entry);
      Files.copy(repositoryFile.toPath(), taos);
      taos.closeArchiveEntry();
    } finally {
      Files.delete(repositoryFile.toPath());
    }
  }

  private void writeEnvironmentData(TarArchiveOutputStream taos) throws IOException {
    byte[] envBytes = generator.generate();
    TarArchiveEntry entry = new TarArchiveEntry("scm-environment.xml");
    entry.setSize(envBytes.length);
    taos.putArchiveEntry(entry);
    taos.write(envBytes);
    taos.closeArchiveEntry();
  }

  private void writeStoreData(Repository repository, TarArchiveOutputStream taos) throws IOException {
    File metadata = Files.createFile(Paths.get("metadata")).toFile();
    try (FileOutputStream metadataFos = new FileOutputStream(metadata)) {
      storeExporter.export(repository, metadataFos);
      TarArchiveEntry entry = new TarArchiveEntry("scm-metadata.tar");
      entry.setSize(metadata.length());
      taos.putArchiveEntry(entry);
      Files.copy(metadata.toPath(), taos);
      taos.closeArchiveEntry();
    } finally {
      Files.delete(metadata.toPath());
    }
  }
}
