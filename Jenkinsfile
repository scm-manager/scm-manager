#!groovy
@Library('github.com/cloudogu/ces-build-lib@9aadeeb')
import com.cloudogu.ces.cesbuildlib.*

node() { // No specific label

  properties([
    // Keep only the last 10 build to preserve space
    buildDiscarder(logRotator(numToKeepStr: '10')),
    // Don't run concurrent builds for a branch, because they use the same workspace directory
    disableConcurrentBuilds()
  ])

  String defaultEmailRecipients = env.EMAIL_SCM_RECIPIENTS

  catchError {

    Maven mvn = new MavenWrapper(this)
    // Maven build specified it must be 1.8.0-101 or newer
    def javaHome = tool 'JDK-1.8.0-101+'

    withEnv(["JAVA_HOME=${javaHome}", "PATH=${env.JAVA_HOME}/bin:${env.PATH}",
             // Give Maven enough memory to do SonarQube analysis
             "MAVEN_OPTS=-Xmx1g"]) {

      stage('Checkout') {
        checkout scm
      }

      stage('Build') {
        // TODO release build only on default? or 2.0.0-M3 -> JavaDoc takes ages
        mvn 'clean install -DskipTests -DperformRelease -Dmaven.javadoc.failOnError=false'
      }

      stage('Unit Test') {
        mvn 'test -Dsonia.scm.test.skip.hg=true'
      }

      stage('SonarQube') {

        def sonarQube = new SonarQube(this, 'sonarcloud.io')

        // TODO move this to ces-build-lib so we can use "sonarqube.analyzeWith(mvn)" here
        analyzeWith(mvn)

        if (!sonarQube.waitForQualityGateWebhookToBeCalled()) {
          currentBuild.result = 'UNSTABLE'
        }
      }
    }
  }

  // Archive Unit and integration test results, if any
  junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/TEST-*.xml,**/target/surefire-reports/TEST-*.xml,**/target/jest-reports/TEST-*.xml'

  // Find maven warnings and visualize in job
  warnings consoleParsers: [[parserName: 'Maven']], canRunOnFailed: true

  mailIfStatusChanged(defaultEmailRecipients)
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
        "-Dsonar.pullrequest.bitbucketcloud.repository=sonarcloudtest "
    } else {
      mvnArgs += " -Dsonar.branch.name=${env.BRANCH_NAME} "
      if (!"default".equals(env.BRANCH_NAME)) {
        // Avoid exception "The main branch must not have a target" on master branch
        mvnArgs += " -Dsonar.branch.target=default "
      }
    }
    mvn "${mvnArgs}"
  }
}
