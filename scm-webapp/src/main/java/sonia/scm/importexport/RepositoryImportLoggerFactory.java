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
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import sonia.scm.NotFoundException;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import javax.inject.Inject;
import java.io.OutputStream;
import java.io.PrintStream;

public class RepositoryImportLoggerFactory {

  private final DataStoreFactory dataStoreFactory;

  @Inject
  RepositoryImportLoggerFactory(DataStoreFactory dataStoreFactory) {
    this.dataStoreFactory = dataStoreFactory;
  }

  RepositoryImportLogger createLogger() {
    return new RepositoryImportLogger(dataStoreFactory.withType(RepositoryImportLog.class).withName("imports").build());
  }

  public void getLog(String logId, OutputStream out) {
    DataStore<RepositoryImportLog> importStore = dataStoreFactory.withType(RepositoryImportLog.class).withName("imports").build();
    RepositoryImportLog log = importStore.getOptional(logId).orElseThrow(() -> new NotFoundException("Log", logId));
    checkPermission(log);
    PrintStream printStream = new PrintStream(out);
    log.toLogHeader().forEach(printStream::println);
    log.getEntries()
      .stream()
      .map(RepositoryImportLog.Entry::toLogMessage)
      .forEach(printStream::println);
  }

  private void checkPermission(RepositoryImportLog log) {
    Subject subject = SecurityUtils.getSubject();
    if (!subject.isPermitted("only:admin:allowed") && !subject.getPrincipal().toString().equals(log.getUserId())) {
      throw new AuthorizationException("not permitted");
    }
  }
}
