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
import org.eclipse.jetty.client.api.ContentResponse
import org.eclipse.jetty.client.util.BasicAuthentication
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HttpUploadTask extends DefaultTask {

  private static final Logger LOG = LoggerFactory.getLogger(HttpUploadTask)

  @Input
  File artifact

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

  void artifact(Object object) {
    println object
    println object.class
    if (object instanceof AbstractArchiveTask) {
      artifact = object.getArchiveFile().get().asFile
    } else if (object instanceof File) {
      artifact = object
    } else if (object instanceof String) {
      artifact = new File(object)
    } else {
      throw new IllegalArgumentException("unknown artifact type")
    }
  }

  @TaskAction
  void upload() {
    URI uri = URI.create(createURL())
    LOG.info("Upload {} with {} to {}", artifact, method, uri)

    SslContextFactory.Client sslContextFactory = new SslContextFactory.Client()
    HttpClient client = new HttpClient(sslContextFactory)
    client.getAuthenticationStore().addAuthenticationResult(new BasicAuthentication.BasicResult(
      uri, username, password
    ))
    client.start()

    try {
      ContentResponse response = client.newRequest(uri)
        .method(method)
        .file(artifact.toPath())
        .send()

      int status = response.getStatus()
      if (status >= 300) {
        throw new GradleException("failed to upload artifact, server returned ${status}")
      } else {
        LOG.info("successfully upload artifact, server returned with status {}", status)
      }
    } finally {
      client.stop()
    }
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
