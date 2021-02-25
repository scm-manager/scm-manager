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

import org.apache.shiro.SecurityUtils;
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
      e.printStackTrace();
    }
  }

  private OutputStream getBlobOutputStream() {
    try {
      return blob.getOutputStream();
    } catch (IOException e) {
      e.printStackTrace();
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
      e.printStackTrace();
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
