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
import java.io.OutputStream;

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
    try (OutputStream outputStream = blob.getOutputStream()) {
      Resources.copy(
        Resources.getResource("sonia/scm/importexport/importLog.blob"),
        outputStream);
    }
    blob.commit();
  }
}
