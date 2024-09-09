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

import com.moowork.gradle.node.task.NodeTask
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

class RunTask extends DefaultTask {

  @Input
  boolean frontend = true

  @Input
  boolean waitForCompletion = true

  @Nested
  ScmServerExtension extension

  @Input
  @Option(option = 'analyze-bundles', description = 'Include Webpack Bundle Analyzer Plugin')
  boolean analyzeBundles = false

  @Input
  @Option(option = 'debug-jvm', description = 'Start ScmServer suspended and listening on debug port (default: 5005)')
  boolean debugJvm = false

  @Input
  @Option(option = 'debug-wait', description = 'Wait until a debugger has connected')
  boolean debugWait = false

  @Input
  @Option(option = 'debug-port', description = 'Port for debugger')
  String debugPort = "5005"

  @Input
  @Option(option = 'configFileDirectory', description = 'Path to config file')
  String configFileDirectory = ''

  @TaskAction
  void exec() {
    List<Closure<Void>> actions = new ArrayList<>()

    // resolve the classpath on a thread which is controlled by gradle
    Configuration serverClasspath = project.buildscript.configurations.classpath
    serverClasspath.resolve()

    actions.add(createBackend(serverClasspath))
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
    for (int i = 0; i < retries; i++) {
      try {
        URL urlConnect = new URL("http://localhost:${extension.port}/scm/api/v2")
        URLConnection conn = (HttpURLConnection) urlConnect.openConnection()
        if (conn.getResponseCode() == 200) {
          return
        }
      } catch (IOException ex) {
        System.out.println("scm-server not reachable, retrying...")
      }
      Thread.sleep(500)
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

  private Closure<Void> createBackend(Configuration serverClasspath) {
    Map<String, String> scmProperties = System.getProperties().findAll { e ->
      {
        return e.key.startsWith("scm") || e.key.startsWith("sonia")
      }
    }

    def runProperties = new HashMap<String, String>(scmProperties)
    runProperties.put("user.home", extension.getHome())
    runProperties.put("scm.initialPassword", "scmadmin")
    runProperties.put("scm.workingCopyPoolStrategy", "sonia.scm.repository.work.SimpleCachingWorkingCopyPool")

    return {
      project.javaexec {
        mainClass.set(ScmServer.name)
        jvmArgs("-Xverify:none")
        args(new File(project.buildDir, 'server/config.json').toString())
        environment 'NODE_ENV', 'development'
        environment 'SCM_WEBAPP_HOMEDIR', extension.getHome()
        classpath serverClasspath
        if (configFileDirectory != '') {
          classpath configFileDirectory
        }
        systemProperties = runProperties
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
    def frontend = project.tasks.create('boot-frontend', NodeTask) {
      script = new File(project.rootProject.projectDir, 'scripts/turbo-runner.js')
      args = ['run', 'serve', '--filter=@scm-manager/ui-webapp']
      environment = [
        'NODE_ENV': 'development',
        'ANALYZE_BUNDLES': analyzeBundles
      ]
    }
    return {
      frontend.exec()
    }
  }

}
