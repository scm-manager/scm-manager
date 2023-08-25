#!groovy
pipeline {

  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
    disableConcurrentBuilds()
  }

  agent {
    docker {
      image 'scmmanager/java-build:11.0.15_10-2'
      args '-v /var/run/docker.sock:/var/run/docker.sock --group-add 998'
      label 'scmm'
    }
  }

  environment {
    HOME = "${env.WORKSPACE}"
    SONAR_USER_HOME = "${env.WORKSPACE}/.sonar"
  }

  stages {

    stage('Set Version') {
      when {
        anyOf {
          branch pattern: 'release/*', comparator: 'GLOB'
          branch pattern: 'hotfix/*', comparator: 'GLOB'
        }
      }
      steps {
        // read version from branch, set it and commit it
        gradle "setVersion -PnewVersion=${releaseVersion}"
        sh "git add gradle.properties '**.json'"
        sh "git checkout ${env.BRANCH_NAME}"
        commit "Release version ${releaseVersion}"

        // fetch all remotes from origin
        sh 'git config --replace-all "remote.origin.fetch" "+refs/heads/*:refs/remotes/origin/*"'
        sh 'git fetch --all'

        script {
          if (isReleaseBuild()) {
            // checkout, reset and merge
            sh 'git checkout main'
            sh 'git reset --hard origin/main'
            sh "git merge --ff-only ${env.BRANCH_NAME}"
          }
        }

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

    stage('Check') {
      steps {
        catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
          gradle 'check'
        }
        junit allowEmptyResults: true, testResults: '**/build/test-results/test/TEST-*.xml,**/build/test-results/tests/test/TEST-*.xml,**/build/jest-reports/TEST-*.xml'
      }
    }

    // in parallel with check?
    stage('Integration Tests') {
      steps {
        // TODO remove obligatory rerun flag when flappy tests have been fixed
        gradle '-PrerunIntegrationTests integrationTest'
        junit allowEmptyResults: true, testResults: 'scm-it/build/test-results/javaIntegrationTests/*.xml,scm-ui/build/reports/e2e/*.xml'
        archiveArtifacts allowEmptyArchive: true, artifacts: 'scm-ui/e2e-tests/cypress/videos/*.mp4'
        archiveArtifacts allowEmptyArchive: true, artifacts: 'scm-ui/e2e-tests/cypress/screenshots/**/*.png'
      }
    }

     stage('SonarQube') {
      steps {
        sh 'git config --replace-all "remote.origin.fetch" "+refs/heads/*:refs/remotes/origin/*"'
        sh 'git fetch origin develop'
        script {
          withSonarQubeEnv('sonarcloud.io-scm') {
            String parameters = ' -Dsonar.organization=scm-manager -Dsonar.analysis.scmm-repo=scm-manager/scm-manager'
            if (env.CHANGE_ID) {
              parameters += ' -Dsonar.pullrequest.provider=GitHub'
              parameters += ' -Dsonar.pullrequest.github.repository=scm-manager/scm-manager'
              parameters += " -Dsonar.pullrequest.key=${env.CHANGE_ID}"
              parameters += " -Dsonar.pullrequest.branch=${env.CHANGE_BRANCH}"
              parameters += " -Dsonar.pullrequest.base=${env.CHANGE_TARGET}"
            } else {
              parameters += " -Dsonar.branch.name=${env.BRANCH_NAME}"
              if (env.BRANCH_NAME != "develop") {
                parameters += " -Dsonar.branch.target=develop"
              }
            }
            gradle "sonarqube ${parameters}"
          }
        }
      }
    }

    stage('Deployment') {
      when {
        anyOf {
          branch pattern: 'release/*', comparator: 'GLOB'
          branch pattern: 'hotfix/*', comparator: 'GLOB'
          branch 'develop'
        }
        expression { return isBuildSuccess() }
      }
      steps {
        withPublishEnvironment {
          gradle "-PenablePackaging publish"
        }
      }
    }

    stage('Update tap') {
      when {
        anyOf {
          branch pattern: 'release/*', comparator: 'GLOB'
          branch pattern: 'hotfix/*', comparator: 'GLOB'
        }
        expression { return isBuildSuccess() }
      }
      steps {
        build wait: false, propagate: false, job: 'scm-manager/homebrew-tap/master', parameters: [
          string(name: 'Version', value: getReleaseVersion())
        ]
      }
    }

    stage('Presentation Environment') {
      when {
        branch 'develop'
        expression { return isBuildSuccess() }
      }
      steps {
        script {
          def imageVersion = readFile 'scm-packaging/docker/build/docker.tag'

          build job: 'scm-manager/next-scm.cloudogu.com/master', propagate: false, wait: false, parameters: [
            string(name: 'imageTag', value: imageVersion)
          ]
        }
      }
    }

    stage('Push Tag') {
      when {
        anyOf {
          branch pattern: 'release/*', comparator: 'GLOB'
          branch pattern: 'hotfix/*', comparator: 'GLOB'
        }
        expression { return isBuildSuccess() }
      }
      steps {
        script {
          // push changes back to remote repository
          if (isReleaseBuild()) {
            authGit 'SCM-Manager', 'push origin main --tags'
          } else {
            authGit 'SCM-Manager', "push origin ${env.BRANCH_NAME} --tags"
          }
          authGit 'SCM-Manager', 'push origin --tags'
          authGit 'cesmarvin', "push --tags https://github.com/scm-manager/scm-manager"
        }
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
        sh "git -c user.name='CES Marvin' -c user.email='cesmarvin@cloudogu.com' merge main"

        gradle "setVersionToNextSnapshot"

        sh "git add gradle.properties '**.json'"
        commit 'Prepare for next development iteration'
        authGit 'SCM-Manager', 'push origin develop'
        authGit 'cesmarvin', 'push https://github.com/scm-manager/scm-manager develop'
      }
    }

    stage('Delete Release Branch') {
      when {
        branch pattern: 'release/*', comparator: 'GLOB'
        expression { return isBuildSuccess() }
      }
      steps {
        authGit 'SCM-Manager', "push origin :${env.BRANCH_NAME}"
      }
    }

    stage('Push to GitHub') {
      when {
        anyOf {
          branch pattern: 'develop', comparator: 'GLOB'
          branch pattern: 'main', comparator: 'GLOB'
          branch pattern: 'support/*', comparator: 'GLOB'
        }
        expression { return isBuildSuccess() }
      }
      steps {
        authGit 'cesmarvin', "push -f https://github.com/scm-manager/scm-manager HEAD:${env.BRANCH_NAME}"
        authGit 'cesmarvin', "push --tags https://github.com/scm-manager/scm-manager"
      }
    }

    stage('Send Merge Notification') {
      when {
        branch pattern: 'hotfix/*', comparator: 'GLOB'
        expression { return isBuildSuccess() }
      }
      steps {
        mail to: "scm-team@cloudogu.com",
          subject: "Jenkins Job ${JOB_NAME} - Merge Hotfix Release #${env.BRANCH_NAME}!",
          body: """Please,
          - merge the hotfix release branch ${env.BRANCH_NAME} into main (keep versions of main, merge changelog to keep both versions),
          - merge main into develop (the changelog should have no conflicts),
          - if needed, increase version."""
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

boolean isReleaseBuild() {
  return env.BRANCH_NAME.startsWith('release/')
}

String getReleaseVersion() {
  if (isReleaseBuild()) {
    return env.BRANCH_NAME.substring("release/".length());
  } else {
    return env.BRANCH_NAME.substring("hotfix/".length());
  }
}

void commit(String message) {
  sh "git -c user.name='CES Marvin' -c user.email='cesmarvin@cloudogu.com' commit -m '${message}'"
}

void tag(String version) {
  String message = "Release version ${version}"
  sh "git -c user.name='CES Marvin' -c user.email='cesmarvin@cloudogu.com' tag -m '${message}' ${version}"
}

boolean isBuildSuccess() {
  return currentBuild.result == null || currentBuild.result == 'SUCCESS'
}

void withPublishEnvironment(Closure<Void> closure) {
  withCredentials([
    usernamePassword(credentialsId: 'packages-scm-manager-org', usernameVariable: 'ORG_GRADLE_PROJECT_packagesScmManagerUsername', passwordVariable: 'ORG_GRADLE_PROJECT_packagesScmManagerPassword'),
    string(credentialsId: 'SCM-Manager_ApiKey', variable: 'ORG_GRADLE_PROJECT_ecosystemApiToken'),
    string(credentialsId: 'npm-token-scm-manager', variable: 'ORG_GRADLE_PROJECT_npmToken'),
    file(credentialsId: 'gpg_packages-scm-manager-org', variable: 'GPG_KEY_RING'),
    usernamePassword(credentialsId: 'gpg_packages-scm-manager-org-credentials', usernameVariable: 'GPG_KEY_ID', passwordVariable: 'GPG_KEY_PASSWORD')
  ]) {
    withEnv(["ORG_GRADLE_PROJECT_npmEmail=cesmarvin@cloudogu.com"]) {
      docker.withRegistry('', 'cesmarvin-dockerhub-access-token') {
        closure.call()
      }
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
