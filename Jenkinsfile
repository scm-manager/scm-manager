#!groovy
pipeline {

  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
    disableConcurrentBuilds()
  }

  agent {
    docker {
      image 'scmmanager/java-build:11.0.9.1_1-2'
      args '-v /var/run/docker.sock:/var/run/docker.sock --group-add 998'
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
        gradle "setVersion -PnewVersion=${releaseVersion}"
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
        gradle "-xtest build"
      }
    }

    // in parallel with check?
    stage('Integration Tests') {
      steps {
        gradle 'integrationTest --rerun-tasks'
        junit allowEmptyResults: true, testResults: 'scm-it/build/test-results/javaIntegrationTests/*.xml,scm-ui/build/reports/e2e/*.xml'
        archiveArtifacts allowEmptyArchive: true, artifacts: 'scm-ui/e2e-tests/cypress/videos/*.mp4'
        archiveArtifacts allowEmptyArchive: true, artifacts: 'scm-ui/e2e-tests/cypress/screenshots/**/*.png'
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

void gradle(String command) {
  // setting user home system property, should fix user prefs (?/.java/.prefs ...)
  sh "./gradlew -Duser.home=${env.WORKSPACE} ${command}"
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

void withPublishEnivronment(Closure<Void> closure) {
  withCredentials([
    usernamePassword(credentialsId: 'maven.scm-manager.org', usernameVariable: 'ORG_GRADLE_PROJECT_packagesScmManagerUsername', passwordVariable: 'ORG_GRADLE_PROJECT_packagesScmManagerPassword'),
    usernamePassword(credentialsId: 'hub.docker.com-cesmarvin', usernameVariable: 'ORG_GRADLE_PROJECT_dockerUsername', passwordVariable: 'ORG_GRADLE_PROJECT_dockerPassword'),
    usernamePassword(credentialsId: 'cesmarvin-github', usernameVariable: 'ORG_GRADLE_PROJECT_gitHubUsername', passwordVariable: 'ORG_GRADLE_PROJECT_gitHubApiToken'),
    string(credentialsId: 'cesmarvin_npm_token', variable: 'ORG_GRADLE_PROJECT_npmToken'),
    file(credentialsId: 'oss-gpg-secring', variable: 'GPG_KEY_RING'),
    usernamePassword(credentialsId: 'oss-keyid-and-passphrase', usernameVariable: 'GPG_KEY_ID', passwordVariable: 'GPG_KEY_PASSWORD')
  ]) {
    withEnv(["ORG_GRADLE_PROJECT_npmEmail=cesmarvin@cloudogu.com"]) {
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
