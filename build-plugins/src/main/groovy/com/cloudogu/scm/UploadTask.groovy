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
