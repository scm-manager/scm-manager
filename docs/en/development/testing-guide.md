---
title: Testing Guide
subtitle: Howto write tests for SCM-Manager
displayToc: false
---

SCM-Manager supports many kinds of tests to prove the functionality and integrity of the source code. 
This guide gives a peek how the different classes and components should be tested.

## Java Unit Tests
### JUnit 4
SCM-Manager is already about 10 years in development. There are still many unit tests written with JUnit 4. 
Whenever such areas are being worked on, the tests should be migrated to JUnit 5. 

### JUnit 5
Adding or changing logic in Java classes must always be backed up with meaningful unit tests. 
We strongly recommend using JUnit 5 with the optional extensions [MockitoExtension.class](https://github.com/mockito/mockito) for mocking 
and [ShiroExtension.class](https://github.com/sdorra/junit-shiro-extension) for permission handling. 
For assertions we use [AssertJ](https://assertj.github.io/doc/#overview-what-is-assertj) most of the times.

- [Example Link Enricher Tests](https://github.com/scm-manager/scm-manager/blob/af8980de19b580f80c8ec5f72428071d6b45dbfb/scm-plugins/scm-git-plugin/src/test/java/sonia/scm/api/v2/resources/RepositoryLinkEnricherTest.java)
- [Example REST Resource Tests](https://github.com/scm-manager/scm-manager/blob/8f91c217fc7c6ef8f7b454dd40e4656737617859/scm-webapp/src/test/java/sonia/scm/api/v2/resources/ConfigResourceTest.java)

## Frontend Component Tests
For frontend components tests we differ between UI tests and API tests.

### Storybook
Writing fitting and responsive components can be easily done using our [Storybook](https://github.com/storybookjs/storybook) in the module `ui-components`.
The storybook gives a nice overview of the existing components and prevents visual regression. 
Just start it with `yarn storybook` inside `ui-components` and have a look at the existing code on how our stories are written.

- [Example Stories](https://github.com/scm-manager/scm-manager/blob/9e45d8255d75712269407ebf932cb2a8990a7c17/scm-ui/ui-components/src/repos/RepositoryEntry.stories.tsx)

### Jest Tests
For logic tests and api call tests we prefer [jest](https://github.com/facebook/jest) respectively [fetch-mock-jest](https://github.com/wheresrhys/fetch-mock-jest).
- [Example Jest Tests](https://github.com/scm-manager/scm-manager/blob/9e45d8255d75712269407ebf932cb2a8990a7c17/scm-ui/ui-webapp/src/users/components/userValidation.test.ts)
- [Example Fetch-Mock-Jest Tests](https://github.com/scm-manager/scm-manager/blob/9e45d8255d75712269407ebf932cb2a8990a7c17/scm-ui/ui-api/src/admin.test.ts)

## Integration Tests

### Java
Writing integration tests in Java we also suggest JUnit 5. 
This kind of integration tests are used to test whole modules via the REST-API.
Parameterized tests can check integrations with different inputs/configs.

- [Example java integration test](https://github.com/scm-manager/scm-manager/blob/5f887d4fa8d11f471de981ec78f5e112d1863fcf/scm-it/src/test/java/sonia/scm/it/AnonymousAccessITCase.java)

### Integration Test Runner
As we needed to also test integration via the UI/Frontend and especially integrations between different plugins we created our own integration-test-runner.
This test-runner is based on cypress which supports cucumber styled behaviour driven testing. 
Find more about it here: 
- [Integration Test Runner](https://github.com/scm-manager/integration-test-runner)
- [Cypress](https://github.com/cypress-io/cypress)
- [Cypress Cucumber](https://github.com/TheBrainFamily/cypress-cucumber-preprocessor)

Also some test examples:
- [Example Test Features](https://github.com/scm-manager/scm-editor-plugin/blob/d9e1e625d6d36ffddc49a758c4ca23ff0dfaffe9/src/test/e2e/cypress/integration/create.feature)
- [Example Test Definitions](https://github.com/scm-manager/scm-editor-plugin/blob/d9e1e625d6d36ffddc49a758c4ca23ff0dfaffe9/src/test/e2e/cypress/support/step_definitions/scm-editor-plugin.js)
