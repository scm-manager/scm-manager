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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.time.Instant

class TouchFilesTask extends DefaultTask {

  @Input
  Object directory

  @Input
  String extension

  @Input
  long timestamp

  TouchFilesTask() {
    timestamp = Instant.now().toEpochMilli()
    // this task should run always
    outputs.upToDateWhen {
      false
    }
  }

  @TaskAction
  public void execute() {
    if (directory instanceof File) {
      touchDirectory(directory)
    } else if (directory instanceof String) {
      touchDirectory(new File((String) directory))
    }
  }

  private void touchDirectory(File file) {
    if (file.exists()) {
      touch(file)
    }
  }

  private void touch(File file) {
    if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        touch(child)
      }
    } else if (file.getName().endsWith(".${extension}")) {
      file.setLastModified(timestamp)
    }
  }

}
