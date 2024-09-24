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

import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.user.User;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.Instant;

import static java.nio.charset.StandardCharsets.UTF_8;

class RepositoryImportLogger {

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryImportLogger.class);

  private final BlobStore logStore;
  private PrintWriter print;
  private Blob blob;

  RepositoryImportLogger(BlobStore logStore) {
    this.logStore = logStore;
  }

  void start(ImportType importType, Repository repository) {
    User user = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
    blob = logStore.create(repository.getId());
    OutputStream outputStream = getBlobOutputStream();
    writeUser(user, outputStream);
    print = new PrintWriter(outputStream);
    print.printf("Import of repository %s/%s%n", repository.getNamespace(), repository.getName());
    print.printf("Repository type: %s%n", repository.getType());
    print.printf("Imported from: %s%n", importType);
    print.printf("Imported by %s (%s)%n", user.getId(), user.getName());
    print.println();

    addLogEntry("import started");
  }

  private void writeUser(User user, OutputStream outputStream) {
    try {
      outputStream.write(user.getId().getBytes(UTF_8));
      outputStream.write(0);
    } catch (IOException e) {
      LOG.warn("Could not write user to import log blob", e);
    }
  }

  private OutputStream getBlobOutputStream() {
    try {
      return blob.getOutputStream();
    } catch (IOException e) {
      LOG.warn("Could not create logger for import; failed to get output stream from blob", e);
      return new OutputStream() {
        @Override
        public void write(int b) {
          // this is a dummy
        }
      };
    }
  }

  public void finished() {
    step("import finished successfully");
    writeLog();
  }

  public void failed(Exception e) {
    step("import failed (see next log entry)");
    print.println(e.getMessage());
    writeLog();
  }

  public void repositoryCreated(Repository createdRepository) {
    step("created repository: " + createdRepository.getNamespaceAndName());
  }

  public void step(String message) {
    addLogEntry(message);
  }

  private void writeLog() {
    print.flush();
    try {
      blob.commit();
    } catch (IOException e) {
      LOG.warn("Could not commit blob with import log", e);
    }
  }

  private void addLogEntry(String message) {
    print.printf("%s - %s%n", Instant.now(), message);
  }

  public boolean started() {
    return blob != null;
  }

  enum ImportType {
    FULL, URL, DUMP
  }
}
