/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the 'Software'), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package com.cloudogu.scm

import com.moowork.gradle.node.yarn.YarnTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.GradleException
import org.gradle.api.tasks.options.Option

class RunTask extends DefaultTask {

  @Input
  boolean frontend = true

  @Input
  boolean waitForCompletion = true

  @Nested
  ScmServerExtension extension

  @Input
  @Option(option = 'debug-jvm', description = 'Start ScmServer suspended and listening on debug port (default: 5005)')
  boolean debugJvm = false

  @Input
  @Option(option = 'debug-wait', description = 'Wait until a debugger has connected')
  boolean debugWait = false

  @Input
  @Option(option = 'debug-port', description = 'Port for debugger')
  String debugPort = "5005"

  @TaskAction
  void exec() {
    List<Closure<Void>> actions = new ArrayList<>();
    actions.add(createBackend())
    if (frontend) {
      actions.add(createFrontend())
    }
    def threads = start(actions)
    if (waitForCompletion) {
      wait(threads)
    } else {
      waitForPortToBeOpen()
    }
  }

  private void waitForPortToBeOpen() {
    int retries = 180
    for (int i=0; i<retries; i++) {
      try {
        URL urlConnect = new URL("http://localhost:${extension.port}/scm/api/v2");
        URLConnection conn = (HttpURLConnection) urlConnect.openConnection();
        if (conn.getResponseCode() == 200) {
          return
        }
      } catch (IOException ex) {
        Thread.sleep(500)
      }
    }
    throw new GradleException("scm-server not reachable")
  }

  private static void wait(List<Thread> threads) {
    for (Thread thread : threads) {
      thread.join()
    }
  }

  private static List<Thread> start(List<Closure<Void>> actions) {
    return actions.collect({ action ->
      Thread thread = new Thread(action)
      thread.start()
      return thread
    })
  }

  private Closure<Void> createBackend() {
    return {
      project.javaexec {
        mainClass.set(ScmServer.name)
        args(new File(project.buildDir, 'server/config.json').toString())
        environment 'NODE_ENV', 'development'
        classpath project.buildscript.configurations.classpath
        systemProperties = ["user.home": extension.getHome(), "scm.initialPassword": "scmadmin"]
        if (debugJvm) {
          debug = true
          debugOptions {
            enabled = true
            port = Integer.parseInt(debugPort)
            server = true
            suspend = debugWait
          }
        }
      }
    }

  }

  private Closure<Void> createFrontend() {
    def frontend = project.tasks.create('boot-frontend', YarnTask) {
      args = ['run', 'serve']
      environment = [
        'NODE_ENV': 'development'
      ]
    }
    return {
      frontend.exec()
    }
  }

}
