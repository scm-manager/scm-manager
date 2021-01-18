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
