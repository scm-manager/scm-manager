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
import sonia.scm.repository.Repository;
import sonia.scm.store.ExportableStore;
import sonia.scm.store.StoreExporter;

import javax.inject.Inject;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class RepositoryStoreExporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryStoreExporter.class);

  private final StoreExporter storeExporter;

  @Inject
  public RepositoryStoreExporter(StoreExporter storeExporter) {
    this.storeExporter = storeExporter;
  }

  public void export(Repository repository, OutputStream output) {
    try (
      BufferedOutputStream bos = new BufferedOutputStream(output);
      GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
      final TarArchiveOutputStream taos = new TarArchiveOutputStream(gzos)
    ) {
      List<ExportableStore> exportableStores = storeExporter.findExportableStores(repository);

      for (final ExportableStore store : exportableStores) {
        store.export(name -> {
          taos.putArchiveEntry(new TarArchiveEntry("stores/" + store.getType() + "/" + store.getName() + "/" + name));
          return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
              taos.write(b);
            }

            @Override
            public void close() throws IOException {
              taos.closeArchiveEntry();
            }
          };
        });
      }

    } catch (IOException e) {
      LOGGER.error("Could not export repository stores for {}", repository, e);
    }
  }
}
