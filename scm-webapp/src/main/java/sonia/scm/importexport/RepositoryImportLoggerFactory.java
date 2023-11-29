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
