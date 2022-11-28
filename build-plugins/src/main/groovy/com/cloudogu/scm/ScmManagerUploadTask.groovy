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
