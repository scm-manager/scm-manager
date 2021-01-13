#!groovy
pipeline {

  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
    disableConcurrentBuilds()
  }

  agent {
    docker {
      image 'scmmanager/java-build:11.0.9_11.1'
      label 'docker'
    }
  }

  environment {
    HOME = "${env.WORKSPACE}"
    SONAR_USER_HOME = "${env.WORKSPACE}/.sonar"
  }

  stages {

    stage('Set Version') {
      when {
        branch pattern: 'release/*', comparator: 'GLOB'
      }
      steps {
        // read version from branch, set it and commit it
        sh "./gradlew setVersion -PnewVersion ${releaseVersion}"
        sh "git add gradle.properties lerna.json '**.json'"
        commit "Release version ${releaseVersion}"

        // fetch all remotes from origin
        sh 'git config --replace-all "remote.origin.fetch" "+refs/heads/*:refs/remotes/origin/*"'
        sh 'git fetch --all'

        // checkout, reset and merge
        sh 'git checkout master'
        sh 'git reset --hard origin/master'
        sh "git merge --ff-only ${env.BRANCH_NAME}"

        // set tag
        tag releaseVersion
      }
    }

    stage('Build') {
      steps {
        // build without tests
        sh "./gradlew -xtest build"
      }
    }

    stage('Check') {
      steps {
        sh "./gradlew check"
        junit allowEmptyResults: true, testResults: '**/build/test-results/test/TEST-*.xml,**/build/test-results/tests/test/TEST-*.xml,**/build/jest-reports/TEST-*.xml'
      }
    }

    // in parallel with check?
    stage('Integration Tests') {
      steps {
        sh "./gradlew integrationTest"
        junit allowEmptyResults: true, testResults: 'scm-it/build/test-results/javaIntegrationTests/*.xml,scm-ui/build/reports/e2e/*.xml'
        archiveArtifacts allowEmptyArchive: true, artifacts: 'scm-ui/e2e-tests/cypress/videos/*.mp4'
        archiveArtifacts allowEmptyArchive: true, artifacts: 'scm-ui/e2e-tests/cypress/screenshots/**/*.png'
      }
    }

    stage('SonarQube') {
      steps {
        sh 'git config --replace-all "remote.origin.fetch" "+refs/heads/*:refs/remotes/origin/*"'
        sh 'git fetch origin master'
        script {
          withSonarQubeEnv('sonarcloud.io-scm') {
            String sonar = "sonarqube -Dsonar.organization=scm-manager -Dsonar.branch.name=${env.BRANCH_NAME}"
            if (env.BRANCH_NAME != "master") {
              sonar += " -Dsonar.branch.target=master"
            }
            sh "./gradlew sonarqube"
          }
        }
      }
    }

    stage('Deployment') {
      when {
        branch pattern: 'release/*', comparator: 'GLOB'
        // TODO or develop
        expression { return isBuildSuccess() }
      }
      steps {
        withPublishProperies {
          sh "./gradlew ${PUBLISH_PROPERTIES}"
        }
      }
    }

    stage('Push Tag') {
      when {
        branch pattern: 'release/*', comparator: 'GLOB'
        expression { return isBuildSuccess() }
      }
      steps {
        // push changes back to remote repository
        authGit 'cesmarvin-github', 'push origin master --tags'
        authGit 'cesmarvin-github', 'push origin --tags'
      }
    }


    stage('Set Next Version') {
      when {
        branch pattern: 'release/*', comparator: 'GLOB'
        expression { return isBuildSuccess() }
      }
      steps {
        sh returnStatus: true, script: "git branch -D develop"
        sh "git checkout develop"
        sh "git merge master"

        sh "./gradlew setVersionToNextSnapshot"

        sh "git add gradle.properties lerna.json '**.json'"
        commit 'Prepare for next development iteration'
        authGit 'cesmarvin-github', 'push origin develop'
      }
    }

    stage('Delete Release Branch') {
      when {
        branch pattern: 'release/*', comparator: 'GLOB'
        expression { return isBuildSuccess() }
      }
      steps {
        authGit 'cesmarvin-github', "push origin :${env.BRANCH_NAME}"
      }
    }

  }

  post {
    failure {
      mail to: "scm-team@cloudogu.com",
        subject: "Jenkins Job ${JOB_NAME} - Build #${BUILD_NUMBER} - ${currentBuild.currentResult}!",
        body: "Check console output at ${BUILD_URL} to view the results."
    }
    unstable {
      mail to: "scm-team@cloudogu.com",
        subject: "Jenkins Job ${JOB_NAME} - Build #${BUILD_NUMBER} - ${currentBuild.currentResult}!",
        body: "Check console output at ${BUILD_URL} to view the results."
    }
    fixed {
      mail to: "scm-team@cloudogu.com",
        subject: "Jenkins Job ${JOB_NAME} - Is back to normal with Build #${BUILD_NUMBER}",
        body: "Check console output at ${BUILD_URL} to view the results."
    }
  }
}

String getReleaseVersion() {
  return env.BRANCH_NAME.substring("release/".length());
}

void commit(String message) {
  sh "git -c user.name='CES Marvin' -c user.email='cesmarvin@cloudogu.com' commit -m '${message}'"
}

void tag(String version) {
  String message = "Release version ${version}"
  sh "git -c user.name='CES Marvin' -c user.email='cesmarvin@cloudogu.com' tag -m '${message}' ${version}"
}

void isBuildSuccess() {
  return currentBuild.result == null || currentBuild.result == 'SUCCESS'
}

void withPublishProperies(Closure<Void> closure) {
  withCredentials([
    usernamePassword(credentialsId: 'maven.scm-manager.org', passwordVariable: 'PACKAGES_PASSWORD', usernameVariable: 'PACKAGES_USERNAME'),
    usernamePassword(credentialsId: 'hub.docker.com-cesmarvin', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME'),
    string(credentialsId: 'cesmarvin_npm_token', variable: 'NPM_TOKEN'),
    file(credentialsId: 'oss-gpg-secring', variable: 'GPG_KEYRING'),
    usernamePassword(credentialsId: 'oss-keyid-and-passphrase', usernameVariable: 'GPG_KEY_ID', passwordVariable: 'GPG_KEY_PASSPHRASE')
  ]) {
    String properties = "-PpackagesScmManagerUsername=${PACKAGES_USERNAME} -PpackagesScmManagerPassword=${PACKAGES_PASSWORD}"
    properties += " -PdockerUsername=${DOCKER_USERNAME} -PdockerPassword=${DOCKER_PASSWORD}"
    properties += " -PnpmEmail=cesmarvin@cloudogu.com -PnpmToken=${NPM_TOKEN}"
    properties += " -Psigning.secretKeyRingFile=${GPG_KEYRING} -Psigning.keyId=${GPG_KEY_ID} -Psigning.password=${GPG_KEY_PASSPHRASE}"
    withEnv(["PUBLISH_PROPERTIES=\"${properties}\""]) {
      closure.call()
    }
  }
}

void authGit(String credentials, String command) {
  withCredentials([
    usernamePassword(credentialsId: credentials, usernameVariable: 'AUTH_USR', passwordVariable: 'AUTH_PSW')
  ]) {
    sh "git -c credential.helper=\"!f() { echo username='\$AUTH_USR'; echo password='\$AUTH_PSW'; }; f\" ${command}"
  }
}
