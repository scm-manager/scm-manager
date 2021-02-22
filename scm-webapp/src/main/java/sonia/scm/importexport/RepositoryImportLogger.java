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
import sonia.scm.importexport.RepositoryImportLog.ImportType;
import sonia.scm.repository.Repository;
import sonia.scm.store.DataStore;
import sonia.scm.user.User;

class RepositoryImportLogger {

  private final DataStore<RepositoryImportLog> logStore;
  private RepositoryImportLog log;
  private String logId;

  RepositoryImportLogger(DataStore<RepositoryImportLog> logStore) {
    this.logStore = logStore;
  }

  void start(ImportType importType, Repository repository) {
    User user = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
    log = new RepositoryImportLog();
    log.setType(importType);
    log.setUserId(user.getId());
    log.setUserName(user.getName());
    log.setRepositoryType(repository.getType());
    log.setNamespace(repository.getNamespace());
    log.setName(repository.getName());
    logId = logStore.put(log);
    addLogEntry(new RepositoryImportLog.Entry("import started"));
  }


  public void finished() {
    log.setSuccess(true);
    step("import finished successfully");
  }

  public void failed(Exception e) {
    log.setSuccess(false);
    step("import failed (see next log entry)");
    step(e.getMessage());
  }

  public void repositoryCreated(Repository createdRepository) {
    log.setNamespace(createdRepository.getNamespace());
    log.setName(createdRepository.getName());
    log.setRepositoryId(createdRepository.getId());
    step("created repository: " + createdRepository.getNamespaceAndName());
  }

  public void step(String message) {
    addLogEntry(new RepositoryImportLog.Entry(message));
  }

  private void addLogEntry(RepositoryImportLog.Entry entry) {
    log.addEntry(entry);
    logStore.put(logId, log);
  }
}
