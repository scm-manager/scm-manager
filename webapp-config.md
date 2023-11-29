# Webapp config options

#### Disables feedback links on frontend page footer

disableFeedback: false

#### Makes jwt token endless to prevent that logged-in users get automatically logged off. Warning: Enabling this option can lead to security issue.

endlessJwt: false

#### Set app stage, e.g. DEVELOPMENT, PRODUCTION, TESTING

stage: PRODUCTION

#### Override app version (for testing purposes only)

versionOverride:

#### Sets path for the working directory which is used for internal repository operations. Empty string defaults to java tmpdir

workdir:

#### Parent config to configure caches

cache:

- dataFileCache:
  enabled: true
- externalGroups:
  maximumSize: 42

  #### Username of initial admin user
  initialUser
  #### Password of initial admin user
  initialPassword
  #### skip initial admin creation
  skipAdminCreation: false

#### Number of async threads

asyncThreads: 4

#### Max seconds to abort async execution

maxAsyncAbortSeconds: 60

#### Central work queue

central-work-queue:
workers: 4

#### Strategy for the working copy pool implementation [NoneCachingWorkingCopyPool, SimpleCachingWorkingCopyPool]

workingCopyPoolStrategy: NoneCachingWorkingCopyPool

#### Amount of "cached" working copies

workingCopyPoolSize: 5

#### Cache xml stores in memory

storeCache:
enabled: true
