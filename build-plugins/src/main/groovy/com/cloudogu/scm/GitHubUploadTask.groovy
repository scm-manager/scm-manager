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
  @Optional
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

    @Internal
    boolean isValid() {
      !Strings.isNullOrEmpty(name) && !Strings.isNullOrEmpty(email)
    }
  }
}
