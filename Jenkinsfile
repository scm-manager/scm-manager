#!groovy

// switch back to a stable tag, after pr 22 is mreged an the next version is released
// see https://github.com/cloudogu/ces-build-lib/pull/22
@Library('github.com/cloudogu/ces-build-lib@7a14da6')
import com.cloudogu.ces.cesbuildlib.*

node('docker') {

  developmentBranch = 'develop'
  mainBranch = 'master'

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
          // we need to run 'yarn install' in order to set version with ui-scripts
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

          // we need to fetch all branches, so we can checkout master and develop later
          sh "git config 'remote.origin.fetch' '+refs/heads/*:refs/remotes/origin/*'"
          sh "git fetch --all"

          // merge release branch into main branch
          sh "git checkout ${mainBranch}"
          sh "git reset --hard origin/${mainBranch}"
          sh "git merge --ff-only ${env.BRANCH_NAME}"

          // set tag
          sh "git -c user.name='CES Marvin' -c user.email='cesmarvin@cloudogu.com' tag -m 'release version ${releaseVersion}' ${releaseVersion}"
        }
      }

      stage('Build') {
        mvn "clean install -DskipTests"
      }

      parallel(
        unitTest: {
          stage('Unit Test') {
            mvn 'test -DskipFrontendBuild -DskipTypecheck -Pcoverage -pl !scm-it -Dmaven.test.failure.ignore=true'
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml,**/target/jest-reports/TEST-*.xml'
          }
        },
        integrationTest: {
          stage('Integration Test') {
            mvn 'verify -Pit -DskipUnitTests -pl :scm-webapp,:scm-it -Dmaven.test.failure.ignore=true'
            junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/TEST-*.xml,**/target/cypress-reports/TEST-*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'scm-ui/e2e-tests/cypress/videos/*.mp4'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'scm-ui/e2e-tests/cypress/screenshots/**/*.png'
          }
        }
      )

      stage('SonarQube') {
        def sonarQube = new SonarCloud(this, [sonarQubeEnv: 'sonarcloud.io-scm', sonarOrganization: 'scm-manager', integrationBranch: 'develop'])
        sonarQube.analyzeWith(mvn)
      }

      if (isBuildSuccessful() && (isDevelopmentBranch() || isReleaseBranch())) {
        def commitHash = git.getCommitHash()

        def imageVersion = mvn.getVersion()
        if (imageVersion.endsWith('-SNAPSHOT')) {
          imageVersion = imageVersion.replace('-SNAPSHOT', "-${commitHash.substring(0,7)}-${BUILD_NUMBER}")
        }

        stage('Deployment') {
          // configuration for docker deployment
          mvn.useRepositoryCredentials([
            id: 'docker.io',
            credentialsId: 'hub.docker.com-cesmarvin'
          ])

          mvn.useRepositoryCredentials([
            id: 'github.com/scm-manager/website',
            credentialsId: 'cesmarvin-github'
          ])

          // deploy java artifacts
          mvn.useDeploymentRepository([
            id: 'packages.scm-manager.org',
            url: 'https://packages.scm-manager.org',
            credentialsId: 'maven.scm-manager.org',
            snapshotRepository: '/repository/snapshots/',
            releaseRepository: '/repository/releases/',
            type: 'Configurable'
          ])
          mvn.deployToNexusRepository()

          // deploy frontend bits
          withCredentials([string(credentialsId: 'cesmarvin_npm_token', variable: 'NPM_TOKEN')]) {
            writeFile encoding: 'UTF-8', file: '.npmrc', text: "//registry.npmjs.org/:_authToken='${NPM_TOKEN}'"
            writeFile encoding: 'UTF-8', file: '.yarnrc', text: '''
              registry "https://registry.npmjs.org/"
              always-auth true
              email cesmarvin@cloudogu.com
            '''.trim()

            // we are tricking lerna by pretending that we are not a git repository
            sh "mv .git .git.disabled"
            try {
              mvn "-pl :scm-ui buildfrontend:run@deploy"
            } finally {
              sh "mv .git.disabled .git"
            }
          }

          // deploy packages
          withGPGEnvironment {
            mvn "-Dgpg.scm.keyring='${GPG_KEYRING}' -Dgpg.scm.key='${GPG_KEY_ID}' -Dgpg.scm.passphrase='${GPG_KEY_PASSPHRASE}' -Ppackaging -rf :scm-packaging deploy"
          }
        }

        stage('Presentation Environment') {
          // we don't use developmentBranch, because we only want the lastest version of develop branch on
          // next-scm. We don't want a support branch or something similar on the presentation environment.
          if ("develop".equals(env.BRANCH_NAME)) {
            build job: 'scm-manager/next-scm.cloudogu.com', propagate: false, wait: false, parameters: [
              string(name: 'changeset', value: commitHash),
              string(name: 'imageTag', value: imageVersion)
            ]
          }
        }

        if (isReleaseBranch()) {
          stage('Update Repository') {

            // merge changes into develop
            sh "git checkout ${developmentBranch}"

            // TODO what if we have a conflict
            // e.g.: someone has edited the changelog during the release
            if (!developmentBranch.equals(mainBranch)) {
              sh "git merge ${mainBranch}"
            }


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
              sh "git -c credential.helper=\"!f() { echo username='\$GIT_AUTH_USR'; echo password='\$GIT_AUTH_PSW'; }; f\" push origin ${mainBranch} --tags"
              if (!developmentBranch.equals(mainBranch)) {
                sh "git -c credential.helper=\"!f() { echo username='\$GIT_AUTH_USR'; echo password='\$GIT_AUTH_PSW'; }; f\" push origin develop --tags"
              }
              sh "git -c credential.helper=\"!f() { echo username='\$GIT_AUTH_USR'; echo password='\$GIT_AUTH_PSW'; }; f\" push origin :${env.BRANCH_NAME}"
            }
          }
        }
      }
    }

    mailIfStatusChanged(git.commitAuthorEmail)
  }
}

String developmentBranch
String mainBranch

Maven setupMavenBuild() {
  MavenWrapperInDocker mvn = new MavenWrapperInDocker(this, "scmmanager/java-build:11.0.9_11.1")
  mvn.enableDockerHost = true

  // disable logging durring the build
  def logConf = "scm-webapp/src/main/resources/logback.ci.xml"
  mvn.additionalArgs += " -Dlogback.configurationFile=${logConf}"
  mvn.additionalArgs += " -Dscm-it.logbackConfiguration=${logConf}"
  mvn.additionalArgs += " -Dsonar.coverage.exclusions=**/*.test.ts,**/*.test.tsx,**/*.stories.tsx"

  if (isDevelopmentBranch() || isReleaseBranch()) {
    // Release starts javadoc, which takes very long, so do only for certain branches
    mvn.additionalArgs += ' -DperformRelease'
    // JDK8 is more strict, we should fix this before the next release. Right now, this is just not the focus, yet.
    mvn.additionalArgs += ' -Dmaven.javadoc.failOnError=false'
  }
  return mvn
}

boolean isReleaseBranch() {
  return env.BRANCH_NAME.startsWith("release/");
}

String getReleaseVersion() {
  return env.BRANCH_NAME.substring("release/".length());
}

boolean isDevelopmentBranch() {
  return developmentBranch.equals(env.BRANCH_NAME)
}

void withGPGEnvironment(def closure) {
  withCredentials([
    file(credentialsId: 'oss-gpg-secring', variable: 'GPG_KEYRING'),
    usernamePassword(credentialsId: 'oss-keyid-and-passphrase', usernameVariable: 'GPG_KEY_ID', passwordVariable: 'GPG_KEY_PASSPHRASE')
  ]) {
    closure.call()
  }
}
