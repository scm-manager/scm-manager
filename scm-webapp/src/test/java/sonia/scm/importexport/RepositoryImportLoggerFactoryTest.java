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

import org.junit.jupiter.api.Test;
import sonia.scm.NotFoundException;
import sonia.scm.store.InMemoryDataStore;
import sonia.scm.store.InMemoryDataStoreFactory;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepositoryImportLoggerFactoryTest {

  private final InMemoryDataStore<RepositoryImportLog> store = new InMemoryDataStore<>();
  private final RepositoryImportLoggerFactory factory = new RepositoryImportLoggerFactory(new InMemoryDataStoreFactory(store));

  @Test
  void shouldReadLog() {
    RepositoryImportLog log = new RepositoryImportLog();
    log.setRepositoryType("git");
    log.setNamespace("hitchhiker");
    log.setName("HeartOfGold");
    log.setUserId("dent");
    log.setUserName("Arthur Dent");
    log.setSuccess(true);

    log.addEntry(new RepositoryImportLog.Entry("import started"));
    log.addEntry(new RepositoryImportLog.Entry("import finished"));

    store.put("42", log);

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    factory.getLog("42", out);

    assertThat(out).asString().contains(
      "Import of repository hitchhiker/HeartOfGold",
      "Repository type: null",
      "Imported from: null",
      "Imported by dent (Arthur Dent)",
      "Finished successful"
    )
      .containsPattern(".+ - import started")
      .containsPattern(".+ - import finished");
  }

  @Test
  void shouldThrowNotFoundExceptionForMissingLog() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    assertThrows(NotFoundException.class, () -> factory.getLog("42", out));
  }
}
