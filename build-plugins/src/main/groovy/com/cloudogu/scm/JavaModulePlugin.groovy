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
