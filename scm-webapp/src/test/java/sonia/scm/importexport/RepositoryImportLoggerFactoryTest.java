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

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.NotFoundException;
import sonia.scm.store.InMemoryDataStore;
import sonia.scm.store.InMemoryDataStoreFactory;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RepositoryImportLoggerFactoryTest {

  private final Subject subject = mock(Subject.class);

  private final InMemoryDataStore<RepositoryImportLog> store = new InMemoryDataStore<>();
  private final RepositoryImportLoggerFactory factory = new RepositoryImportLoggerFactory(new InMemoryDataStoreFactory(store));

  @BeforeEach
  void initSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void cleanupSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldReadLogForExportingUser() {
    when(subject.getPrincipal()).thenReturn("dent");

    createLog();

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
  void shouldReadLogForAdmin() {
    when(subject.getPrincipal()).thenReturn("trillian");
    when(subject.isPermitted(anyString())).thenReturn(true);

    createLog();

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

  @Test
  void shouldFailWithoutPermission() {
    when(subject.getPrincipal()).thenReturn("trillian");
    createLog();

    doThrow(AuthorizationException.class).when(subject).checkPermission("only:admin:allowed");

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    assertThrows(AuthorizationException.class, () -> factory.getLog("42", out));
  }

  private void createLog() {
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
  }
}
