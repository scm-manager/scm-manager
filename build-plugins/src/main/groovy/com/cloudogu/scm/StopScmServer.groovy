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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

class StopScmServer extends DefaultTask {
  
  private ScmServerExtension extension

  @Nested
  ScmServerExtension getExtension() {
    return extension
  }

  void setExtension(ScmServerExtension extension) {
    this.extension = extension
  }

  @TaskAction
  void exec() {
    URL url = new URL("http://localhost:${extension.port}/shutdown?token=_shutdown_")
    try {
      HttpURLConnection connection = (HttpURLConnection )url.openConnection()
      connection.setRequestMethod("POST")
      // ???
      connection.getResponseCode()
    } catch (IOException ex) {
      // already closed ?
    }
  }

}
