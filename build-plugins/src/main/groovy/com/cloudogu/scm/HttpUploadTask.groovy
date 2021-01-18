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


package com.cloudogu.scm

import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.client.api.Request
import org.eclipse.jetty.client.util.BasicAuthentication
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HttpUploadTask extends UploadTask {

  private static final Logger LOG = LoggerFactory.getLogger(HttpUploadTask)

  @Input
  String snapshotUrl

  @Input
  String releaseUrl

  @Input
  String method = "POST"

  @Input
  @Optional
  String username

  @Input
  @Optional
  String password

  HttpUploadTask() {
    // http upload ist not cacheable
    outputs.upToDateWhen {
      false
    }
  }

  @Override
  Request createRequest(HttpClient client) {
    URI uri = URI.create(createURL())
    LOG.info("Upload {} with {} to {}", artifact, method, uri)

    client.getAuthenticationStore().addAuthenticationResult(new BasicAuthentication.BasicResult(
      uri, username, password
    ))

    return client.newRequest(uri)
      .method(method)
      .file(artifact.toPath())
  }

  private String createURL() {
    String repositoryUrl = createRepositoryUrl()
    if ("PUT".equals(method)) {
      if (!repositoryUrl.endsWith("/")) {
        repositoryUrl += "/"
      }
      return repositoryUrl + artifact.name
    }
    return repositoryUrl
  }

  private String createRepositoryUrl() {
    if (project.version.contains("SNAPSHOT")) {
      return snapshotUrl
    }
    return releaseUrl
  }

}
