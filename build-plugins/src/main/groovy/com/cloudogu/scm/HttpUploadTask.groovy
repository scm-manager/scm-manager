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
