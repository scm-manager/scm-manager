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

import com.google.common.base.Strings
import groovy.json.JsonOutput
import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.client.api.Request
import org.eclipse.jetty.client.util.StringContentProvider
import org.eclipse.jetty.http.HttpMethod
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

import java.nio.charset.StandardCharsets

class ScmManagerUploadTask extends UploadTask {

  @Input
  @Optional
  String apiToken

  @Input
  String server

  @Input
  String namespace

  @Input
  String repo

  @Input
  String path

  @Input
  String branch = "master"

  @Input
  String message

  @Override
  Request createRequest(HttpClient client) {
    String[] pathComponents = path.split("/")
    String filename = pathComponents[pathComponents.length - 1]
    String directory = path.substring(0, path.length() - filename.length() - 1)
    Request request = client.newRequest("${server}/api/v2/edit/${namespace}/${repo}/create/${directory}")
    request.method(HttpMethod.POST)
    request.header("Accept", "application/json")
    request.header("Content-Type", "application/json")
    if (!Strings.isNullOrEmpty(apiToken)) {
      request.header("Authorization","Bearer ${apiToken}")
    }

    def body = [
      commitMessage: message,
      branch: branch,
      fileName: filename,
      fileContent: artifact.text
    ]
    request.content(new StringContentProvider(JsonOutput.toJson(body), StandardCharsets.UTF_8))
    return request
  }
}
