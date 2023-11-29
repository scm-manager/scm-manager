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
import org.eclipse.jetty.client.api.Request
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class UploadTask extends DefaultTask {

  private static final Logger LOG = LoggerFactory.getLogger(HttpUploadTask)

  @InputFile
  File artifact

  @Input
  boolean skip = false

  void artifact(Object object) {
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
    if (skip) {
      LOG.warn("upload is skipped")
    } else {
      doUpload()
    }
  }

  private void doUpload() {
    HttpClient client = new HttpClient()
    try {
      client.start()
      ContentResponse response = createRequest(client).send()
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

  abstract Request createRequest(HttpClient client)

}
