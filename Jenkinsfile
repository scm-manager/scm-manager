#!groovy

// Keep the version in sync with the one used in pom.xml in order to get correct syntax completion.
@Library('github.com/cloudogu/ces-build-lib@59d3e94')
import com.cloudogu.ces.cesbuildlib.*

node() { // No specific label

  // Change this as when we go back to default - necessary for proper SonarQube analysis
  mainBranch = "2.0.0-m3"

  properties([
    // Keep only the last 10 build to preserve space
    buildDiscarder(logRotator(numToKeepStr: '10')),
  ])

  catchError {

    Maven mvn = setupMavenBuild()
    // Maven build specified it must be 1.8.0-101 or newer
    def javaHome = tool 'JDK-1.8.0-101+'

    withEnv(["JAVA_HOME=${javaHome}", "PATH=${env.JAVA_HOME}/bin:${env.PATH}"]) {

      stage('Checkout') {
        checkout scm
      }

      stage('Build') {
        mvn 'clean install -DskipTests'
      }

      stage('Unit Test') {
        mvn 'test -Dsonia.scm.test.skip.hg=true -Dmaven.test.failure.ignore=true'
      }

      stage('SonarQube') {

        analyzeWith(mvn)

        if (!waitForQualityGateWebhookToBeCalled()) {
          currentBuild.result = 'UNSTABLE'
        }
      }
    }
  }

  // Archive Unit and integration test results, if any
  junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/TEST-*.xml,**/target/surefire-reports/TEST-*.xml,**/target/jest-reports/TEST-*.xml'

  // Find maven warnings and visualize in job
  warnings consoleParsers: [[parserName: 'Maven']], canRunOnFailed: true

  mailIfStatusChanged(commitAuthorEmail)
}

String mainBranch

Maven setupMavenBuild() {
  Maven mvn = new MavenWrapper(this)

  if (mainBranch.equals(env.BRANCH_NAME)) {
    // Release starts javadoc, which takes very long, so do only for certain branches
    mvn.additionalArgs += ' -DperformRelease'
    // JDK8 is more strict, we should fix this before the next release. Right now, this is just not the focus, yet.
    mvn.additionalArgs += ' -Dmaven.javadoc.failOnError=false'
  }
  return mvn
}

void analyzeWith(Maven mvn) {

  withSonarQubeEnv('sonarcloud.io') {

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
        "-Dsonar.pullrequest.bitbucketcloud.repository=scm-manager "
    } else {
      mvnArgs += " -Dsonar.branch.name=${env.BRANCH_NAME} "
      if (!mainBranch.equals(env.BRANCH_NAME)) {
        // Avoid exception "The main branch must not have a target" on main branch
        mvnArgs += " -Dsonar.branch.target=${mainBranch} "
      }
    }
    mvn "${mvnArgs}"
  }
}

boolean waitForQualityGateWebhookToBeCalled() {
  boolean isQualityGateSucceeded = true
  timeout(time: 2, unit: 'MINUTES') { // Needed when there is no webhook for example
    def qGate = waitForQualityGate()
    echo "SonarQube Quality Gate status: ${qGate.status}"
    if (qGate.status != 'OK') {
      isQualityGateSucceeded = false
    }
  }
  return isQualityGateSucceeded
}

String getCommitAuthorComplete() {
  new Sh(this).returnStdOut 'hg log --branch . --limit 1 --template "{author}"'
}

String getCommitAuthorEmail() {
  def matcher = getCommitAuthorComplete() =~ "<(.*?)>"
  matcher ? matcher[0][1] : ""
}
