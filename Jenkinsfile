#!groovy

// Keep the version in sync with the one used in pom.xml in order to get correct syntax completion.
@Library('github.com/cloudogu/ces-build-lib@1.35.1')
import com.cloudogu.ces.cesbuildlib.*

node('docker') {

  // Change this as when we go back to default - necessary for proper SonarQube analysis
  mainBranch = 'develop'

  properties([
    // Keep only the last 10 build to preserve space
    buildDiscarder(logRotator(numToKeepStr: '10')),
    disableConcurrentBuilds()
  ])

  timeout(activity: true, time: 60, unit: 'MINUTES') {

    Git git = new Git(this)

    catchError {

      Maven mvn = setupMavenBuild()

      stage('Checkout') {
        checkout scm
      }

      if (isReleaseBranch()) {
        stage('Set Version') {
          String releaseVersion = getReleaseVersion();
          // set maven versions
          mvn "versions:set -DgenerateBackupPoms=false -DnewVersion=${releaseVersion}"
          // set versions for ui packages
          // we need to install in order to set version with ui-scripts
          mvn "-pl :scm-ui buildfrontend:install@install"
          mvn "-pl :scm-ui buildfrontend:run@set-version"

          // stage pom changes
          sh "git status --porcelain | sed s/^...// | grep pom.xml | xargs git add"
          // stage package.json changes
          sh "git status --porcelain | sed s/^...// | grep package.json | xargs git add"
          // stage lerna.json changes
          sh "git add lerna.json"

          // commit changes
          sh "git -c user.name='CES Marvin' -c user.email='cesmarvin@cloudogu.com' commit -m 'release version ${releaseVersion}'"

          // merge release branch into master
          sh "git checkout master"
          sh "git merge --ff-only ${env.BRANCH_NAME}"

          // set tag
          sh "git -c user.name='CES Marvin' -c user.email='cesmarvin@cloudogu.com' tag -m 'release version ${releaseVersion}' ${releaseVersion}"
        }
      }

      stage('Build') {
        mvn 'clean install -DskipTests'
      }

      stage('Unit Test') {
        mvn 'test -Pcoverage -Dmaven.test.failure.ignore=true'
        junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml,**/target/jest-reports/TEST-*.xml'
      }

      stage('Integration Test') {
        mvn 'verify -Pit -pl :scm-webapp,:scm-it -Dmaven.test.failure.ignore=true -Dscm.git.core.supportsatomicfilecreation=false'
        junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/TEST-*.xml'
      }

      stage('SonarQube') {

        analyzeWith(mvn)

        if (!waitForQualityGateWebhookToBeCalled()) {
          currentBuild.result = 'UNSTABLE'
        }
      }

      if (isMainBranch() || isReleaseBranch()) {

        stage('Lifecycle') {
          try {
            // failBuildOnNetworkError -> so we can catch the exception and neither fail nor make our build unstable
            nexusPolicyEvaluation iqApplication: selectedApplication('scm'), iqScanPatterns: [[scanPattern: 'scm-server/target/scm-server-app.zip']], iqStage: 'build', failBuildOnNetworkError: true
          } catch (Exception e) {
            echo "ERROR: iQ Server policy eval failed. Not marking build unstable for now."
            echo "ERROR: iQ Server Exception: ${e.getMessage()}"
          }
        }

        if (isBuildSuccessful()) {

          def commitHash = git.getCommitHash()

          def imageVersion = mvn.getVersion()
          if (imageVersion.endsWith('-SNAPSHOT')) {
            imageVersion = imageVersion.replace('-SNAPSHOT', "${commitHash.substring(0,7)}-${BUILD_NUMBER}")
          }

          stage('Archive') {
            archiveArtifacts 'scm-webapp/target/scm-webapp.war'
            archiveArtifacts 'scm-server/target/scm-server-app.*'
          }

          stage('Maven Deployment') {
            // TODO why is the server recreated
            // delete appassembler target, because the maven plugin fails to recreate the tar
            sh "rm -rf scm-server/target/appassembler"

            // deploy java artifacts
            mvn.useRepositoryCredentials([id: 'maven.scm-manager.org', url: 'https://maven.scm-manager.org/nexus', credentialsId: 'oss-jenkins_maven.scm-manager.org', type: 'Nexus2'])
            mvn.deployToNexusRepository()

            // deploy frontend bits
            withCredentials([string(credentialsId: 'cesmarvin_npm_token', variable: 'NPM_TOKEN')]) {
              writeFile encoding: 'UTF-8', file: '.npmrc', text: "//registry.npmjs.org/:_authToken='${NPM_TOKEN}'"
              writeFile encoding: 'UTF-8', file: '.yarnrc', text: '''
                registry "https://registry.npmjs.org/"
                always-auth true
                email cesmarvin@cloudogu.com
              '''.trim()
              mvn "-pl :scm-ui buildfrontend:run@deploy"
            }
          }

          stage('Docker') {
            docker.withRegistry('', 'hub.docker.com-cesmarvin') {
              def image = docker.build('cloudogu/scm-manager')
              image.push(imageVersion)
              if (isReleaseBranch()) {
                image = docker.build('scmmanager/scm-manager')
                image.push(imageVersion)
              }
            }
          }

          stage('Presentation Environment') {
            build job: 'scm-manager/next-scm.cloudogu.com', propagate: false, wait: false, parameters: [
              string(name: 'changeset', value: commitHash),
              string(name: 'imageTag', value: imageVersion)
            ]
          }

          if (isReleaseBranch()) {
            stage('Update Repository') {

              // merge changes into develop
              sh "git checkout develop"
              // TODO what if we have an conflict
              // e.g.: someone has edit the changelog durring the release
              sh "git merge master"

              // set versions for maven packages
              mvn "build-helper:parse-version versions:set -DgenerateBackupPoms=false -DnewVersion='\${parsedVersion.majorVersion}.\${parsedVersion.nextMinorVersion}.0-SNAPSHOT'"
              
              // set versions for ui packages
              mvn "-pl :scm-ui buildfrontend:run@set-version"

              // stage pom changes
              sh "git status --porcelain | sed s/^...// | grep pom.xml | xargs git add"
              // stage package.json changes
              sh "git status --porcelain | sed s/^...// | grep package.json | xargs git add"
              // stage lerna.json changes
              sh "git add lerna.json"

              // commit changes
              sh "git -c user.name='CES Marvin' -c user.email='cesmarvin@cloudogu.com' commit -m 'prepare for next development iteration'"

              // push changes back to remote repository
              withCredentials([usernamePassword(credentialsId: 'cesmarvin-github', usernameVariable: 'GIT_AUTH_USR', passwordVariable: 'GIT_AUTH_PSW')]) {
                sh "git -c credential.helper=\"!f() { echo username='\$GIT_AUTH_USR'; echo password='\$GIT_AUTH_PSW'; }; f\" push origin master --tags"
                sh "git -c credential.helper=\"!f() { echo username='\$GIT_AUTH_USR'; echo password='\$GIT_AUTH_PSW'; }; f\" push origin develop --tags"
                sh "git -c credential.helper=\"!f() { echo username='\$GIT_AUTH_USR'; echo password='\$GIT_AUTH_PSW'; }; f\" push origin :${env.BRANCH_NAME}"
              }
            }
          }

        }
      }
    }

    mailIfStatusChanged(git.commitAuthorEmail)
  }
}

String mainBranch

Maven setupMavenBuild() {
  Maven mvn = new MavenWrapperInDocker(this, "scmmanager/java-build:11.0.6_10")

  if (isMainBranch() || isReleaseBranch()) {
    // Release starts javadoc, which takes very long, so do only for certain branches
    mvn.additionalArgs += ' -DperformRelease'
    // JDK8 is more strict, we should fix this before the next release. Right now, this is just not the focus, yet.
    mvn.additionalArgs += ' -Dmaven.javadoc.failOnError=false'
  }
  return mvn
}

void analyzeWith(Maven mvn) {

  withSonarQubeEnv('sonarcloud.io-scm') {

    String mvnArgs = "${env.SONAR_MAVEN_GOAL} " +
      "-Dsonar.host.url=${env.SONAR_HOST_URL} " +
      "-Dsonar.login=${env.SONAR_AUTH_TOKEN} "

    if (isPullRequest()) {
      echo "Analysing SQ in PR mode"
      mvnArgs += "-Dsonar.pullrequest.base=${env.CHANGE_TARGET} " +
        "-Dsonar.pullrequest.branch=${env.CHANGE_BRANCH} " +
        "-Dsonar.pullrequest.key=${env.CHANGE_ID} " +
        "-Dsonar.pullrequest.provider=bitbucketcloud " +
        "-Dsonar.pullrequest.bitbucketcloud.owner=sdorra " +
        "-Dsonar.pullrequest.bitbucketcloud.repository=scm-manager " +
        "-Dsonar.cpd.exclusions=**/*StoreFactory.java,**/*UserPassword.js "
    } else {
      mvnArgs += " -Dsonar.branch.name=${env.BRANCH_NAME} "
      if (!isMainBranch()) {
        // Avoid exception "The main branch must not have a target" on main branch
        mvnArgs += " -Dsonar.branch.target=${mainBranch} "
      }
    }
    mvn "${mvnArgs}"
  }
}

boolean isReleaseBranch() {
  return env.BRANCH_NAME.startsWith("release/");
}

String getReleaseVersion() {
  return env.BRANCH_NAME.substring("release/".length());
}

boolean isMainBranch() {
  return mainBranch.equals(env.BRANCH_NAME)
}

boolean waitForQualityGateWebhookToBeCalled() {
  boolean isQualityGateSucceeded = true
  timeout(time: 5, unit: 'MINUTES') { // Needed when there is no webhook for example
    def qGate = waitForQualityGate()
    echo "SonarQube Quality Gate status: ${qGate.status}"
    if (qGate.status != 'OK') {
      isQualityGateSucceeded = false
    }
  }
  return isQualityGateSucceeded
}

