---
title: Integration Tests
subtitle: How to run integration tests
displayToc: false
---

You can find the integration tests in the module **scm-it** (and a few still in **scm-webapp**). To run them,
simply start maven with the profile `it`:

```
mvn install -Pit -DskipUnitTests -pl :scm-webapp,:scm-it
```

This will start a jetty server and execute all integration tests in the maven phase `integration-test` using the
normal failsafe plugin. Integration tests are all classes ending with `ITCase` and are written using JUnit.

To develop integration tests, you should start a local server with the **scm-integration-test-plugin**. This plugin is
used as a way to introspect server internals. For example you can register event listeners here and access their
triggers with a REST endpoint. Of course, this plugin is not and should not be installed in productive systems.
You can start the server with this plugin using the following maven call:

```
mvn run -pl :scm-integration-test-plugin
```
