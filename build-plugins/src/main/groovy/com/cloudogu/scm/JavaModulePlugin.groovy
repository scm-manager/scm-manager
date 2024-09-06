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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.toolchain.JavaLanguageVersion

class JavaModulePlugin implements Plugin<Project> {

  void apply(Project project) {
    project.plugins.apply("java")
    project.plugins.apply("maven-publish")
    project.plugins.apply("org.scm-manager.license")

    project.java {
      toolchain {
        languageVersion = JavaLanguageVersion.of(17)
      }
      //TODO Fix javadoc errors which breaks the build
//      withJavadocJar()
      withSourcesJar()
    }

    project.tasks.withType(JavaCompile) {
      options.release = 17
      options.encoding = 'UTF-8'
    }

    project.tasks.withType(Javadoc) {
      failOnError false
    }

    project.sonarqube {
      properties {
        property "sonar.java.source", "17"
      }
    }

    project.afterEvaluate {
      if (project.isCI) {
        project.plugins.apply("jacoco")
        project.jacocoTestReport {
          reports {
            xml.enabled true
          }
        }

        project.tasks.register("update-test-timestamp", TouchFilesTask) {
          directory = new File(project.buildDir, "test-results")
          extension = "xml"
        }

        project.tasks.getByName("test").configure {
          dependsOn "update-test-timestamp"
        }
      }

      project.test {
        useJUnitPlatform()
        if (project.isCI){
          ignoreFailures = true
          finalizedBy project.jacocoTestReport
        }
      }
    }

    project.publishing {
      publications {
        mavenJava(MavenPublication) {
          artifactId project.name
          project.afterEvaluate {
            def component = project.components.findByName("web")
            if (component == null) {
              component = project.components.java
            }
            from component
          }
        }
      }
    }

    project.rootProject.publishing.repositories.each { r ->
      project.publishing.repositories.add(r)
    }

    project.license {
      header project.rootProject.file('LICENSE-HEADER.txt')
      newLine = true
      ignoreNewLine = true
      lineEnding = "\n"

      exclude "**/*.mustache"
      exclude "**/*.json"
      exclude "**/*.ini"
      exclude "**/mockito-extensions/*"
      exclude "**/*.txt"
      exclude "**/*.md"
      exclude "**/*.gz"
      exclude "**/*.zip"
      exclude "**/*.smp"
      exclude "**/*.asc"
      exclude "**/*.png"
      exclude "**/*.jpg"
      exclude "**/*.gif"
      exclude "**/*.dump"

      tasks {
        gradle {
          files.from("build.gradle", "settings.gradle", "gradle.properties")
        }
      }
    }

  }

}
