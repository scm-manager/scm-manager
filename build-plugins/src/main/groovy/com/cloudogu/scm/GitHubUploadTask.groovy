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
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

import java.nio.charset.StandardCharsets

class GitHubUploadTask extends UploadTask {

  @Input
  @Optional
  String apiToken

  @Input
  String owner

  @Input
  String repo

  @Input
  String path

  @Input
  String branch = "master"

  @Input
  String message

  @Nested
  private Author author = new Author()

  @Nested
  private Author committer

  Author getAuthor() {
    return author
  }

  Author getCommitter() {
    return committer
  }

  void author(Closure closure) {
    closure.setDelegate(author)
    closure.call()
  }

  void committer(Closure closure) {
    closure.setDelegate(committer)
    closure.call()
  }

  @Override
  Request createRequest(HttpClient client) {
    Request request = client.newRequest("https://api.github.com/repos/${owner}/${repo}/contents/${path}")
    request.method(HttpMethod.PUT)
    request.header("Accept", "application/vnd.github.v3+json")
    if (!Strings.isNullOrEmpty(apiToken)) {
      request.header("Authorization","Token ${apiToken}")
    }

    def body = [
      message: message,
      branch: branch,
      content: artifact.getBytes().encodeBase64().toString()
    ]
    if (author?.valid) {
      body.author = [
        name: author.name,
        email: author.email
      ]
    }
    if (committer?.valid) {
      body.committer = [
        name: committer.name,
        email: committer.email
      ]
    }
    request.content(new StringContentProvider(JsonOutput.toJson(body), StandardCharsets.UTF_8))
    return request
  }

  static class Author {

    @Input
    @Optional
    String name

    @Input
    @Optional
    String email

    boolean isValid() {
      !Strings.isNullOrEmpty(name) && !Strings.isNullOrEmpty(email)
    }
  }
}
