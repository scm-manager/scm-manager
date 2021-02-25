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

import com.google.common.io.Resources;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.NotFoundException;
import sonia.scm.store.Blob;
import sonia.scm.store.InMemoryBlobStore;
import sonia.scm.store.InMemoryBlobStoreFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RepositoryImportLoggerFactoryTest {

  private final Subject subject = mock(Subject.class);

  private final InMemoryBlobStore store = new InMemoryBlobStore();
  private final RepositoryImportLoggerFactory factory = new RepositoryImportLoggerFactory(new InMemoryBlobStoreFactory(store));

  @BeforeEach
  void initSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void cleanupSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldReadLogForExportingUser() throws IOException {
    when(subject.getPrincipal()).thenReturn("dent");

    createLog();

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    factory.getLog("42", out);

    assertLogReadCorrectly(out);
  }

  @Test
  void shouldReadLogForAdmin() throws IOException {
    when(subject.getPrincipal()).thenReturn("trillian");
    when(subject.isPermitted(anyString())).thenReturn(true);

    createLog();

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    factory.getLog("42", out);

    assertLogReadCorrectly(out);
  }

  private void assertLogReadCorrectly(ByteArrayOutputStream out) {
    assertThat(out).asString().contains(
      "Import of repository hitchhiker/HeartOfGold",
      "Repository type: git",
      "Imported from: URL",
      "Imported by dent (Arthur Dent)",
      "",
      "Thu Feb 25 11:11:07 CET 2021 - import started",
      "Thu Feb 25 11:11:07 CET 2021 - pulling repository from https://github.com/scm-manager/scm-manager",
      "Thu Feb 25 11:11:08 CET 2021 - import finished successfully"
    );
  }

  @Test
  void shouldThrowNotFoundExceptionForMissingLog() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    assertThrows(NotFoundException.class, () -> factory.getLog("42", out));
  }

  @Test
  void shouldFailWithoutPermission() throws IOException {
    when(subject.getPrincipal()).thenReturn("trillian");
    createLog();

    doThrow(AuthorizationException.class).when(subject).checkPermission("only:admin:allowed");

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    assertThrows(AuthorizationException.class, () -> factory.getLog("42", out));
  }

  @SuppressWarnings("UnstableApiUsage")
  private void createLog() throws IOException {
    Blob blob = store.create("42");
    Resources.copy(
      Resources.getResource("sonia/scm/importexport/importLog.blob"),
      blob.getOutputStream());
    blob.commit();
  }
}
