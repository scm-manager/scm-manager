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

import jakarta.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import sonia.scm.NotFoundException;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.util.IOUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RepositoryImportLoggerFactory {

  private final BlobStoreFactory blobStoreFactory;

  @Inject
  RepositoryImportLoggerFactory(BlobStoreFactory blobStoreFactory) {
    this.blobStoreFactory = blobStoreFactory;
  }

  RepositoryImportLogger createLogger() {
    return new RepositoryImportLogger(blobStoreFactory.withName("imports").build());
  }

  public void checkCanReadLog(String logId) throws IOException {
    try (InputStream blob = getBlob(logId)) {
      // nothing to read
    }
  }

  public void getLog(String logId, OutputStream out) throws IOException {
    try (InputStream log = getBlob(logId)) {
      IOUtil.copy(log, out);
    }
  }

  private InputStream getBlob(String logId) throws IOException {
    BlobStore importStore = blobStoreFactory.withName("imports").build();
    InputStream log = importStore
      .getOptional(logId).orElseThrow(() -> new NotFoundException("Log", logId))
      .getInputStream();
    checkPermission(log);
    return log;
  }

  private void checkPermission(InputStream log) throws IOException {
    Subject subject = SecurityUtils.getSubject();
    String logUser = readUserFrom(log);
    if (!subject.isPermitted("only:admin:allowed") && !subject.getPrincipal().toString().equals(logUser)) {
      throw new AuthorizationException("not permitted");
    }
  }

  private String readUserFrom(InputStream log) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int b;
    while ((b = log.read()) > 0) {
      buffer.write(b);
    }
    return new String(buffer.toByteArray(), UTF_8);
  }
}
