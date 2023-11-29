---
title: Decision Table
---

### Lombok

[Project Lombok](https://projectlombok.org/) provides an easy way to generate java beans with

- constructors for no and all fields,
- getters and setters, and
- equals and hash code.

Using this has the following implications:

- The generated code is not explicit (the lombok plugin generates bytecode)
- Especially for `hashCode` and `equals` a lot of code with a high complexity is generated.
  This has implications for test coverage. Even though lombok supports the generation of `Generated`
  annotations (own or jakarta), this has no effect in Sonar.

First, this will be used for data transfer objects (DTO) in the REST endpoints. To avoid the mentioned
complexity of `hashCode` and `equals`, these should not be generated.

The following lombok annotations will be used for DTOs:

- `@Getter`
- `@Setter`
- `@AllArgsConstructor` and `@NoArgsConstructor` (if needed; by default only the default constructor
  will be present. Because this one is necessary for deserialization, this one has to be created
  explicitly when the all-args constructor is declared)

### /repo/ & /repos/ as URI prefixes

The URI-format for accessing a repository, be it with a browser, or cloning/pulling via git/hg/svn, is defined to
be `/repo/:namespace/:name`. The decision was made to allow users to choose namespaces as they please. If there would
not be a prefix, some namespaces (e.g. `user`, `users`) would have to be reserved, since the names are already in use by
SCM Manager itself. The `/repos` URI linked to a list of repositories, as well as operations such as creating a
repositoriy (`/repos/create`).

### Error handling

In v1 error handling was somewhat diverse. Some checks were made explicitly in the web resource classes leading
to direct responses, some were made using exceptions and matching exception mappers.

In v2 we have decided to

1. use bean validation with a mapping to json responses with error messages and http status 400,
1. implement special checks like valid `sortBy` parameters using dedicated exceptions,
1. check ModelObjects internally with an extra `isValid` method (taken from v1) without an extra mapping
   (so that this will lead to 500 errors indicating a missing validation in the web resource)

The following generic exceptions will be used:

- `NotFoundException`,
- `AlreadyExistsException`,
- `ConcurrentModificationException`

Technical errors, for example io errors or other exceptions during the low level repository access are wrapped
in runtime exceptions like `RepositoryException` and will lead to internal server errors (http status 500).

For simple objects like users and groups we don't think that we will need more exceptions.

### Logging

Logging can be cucial when it comes to identify bugs in test or production environments. At implementation time one
cannot foresee all possible error cases and therefore cannot determine with full certanty what informations will be
needed and what can be neglected. Logging only crucial errors leaves the developer with no idea what events might have
lead to the error. On the other hand logging too much will overburden the log, making it harder to handle and maybe
hiding interesting steps.

Therefore it is best practice to be able to select the detail level of informations to log (called the "log level"). To
support this feature SCM-Manager uses [slf4j](https://www.slf4j.org/). Using this library one can log informations with
the following log levels:

* ERROR
* WARN
* INFO
* DEBUG
* TRACE

As a default the log level for SCM-Manager is INFO, so that by default all logs with the levels ERROR, WARN and INFO are
stored. Finer levels can be enabled manually.

### Log levels to use

We have agreed to apply to the following guidelines regarding log levels:

- ERROR should be used for fatal errors that could not be handled by the program and therefore leads to failures for the
  user, for example
  - IO errors reading a database file
  - IO errors accessing repositories
- WARN should be used for errors that could be handled somewhat graceful, but that should be inspected, for example
  - timeouts
- INFO should be used for "major" events like
  - creation of new users, groups and repositories
  - changed plugins
- DEBUG should be used for key events or events desturbing the "happy path", like
  - calling an external tool
  - read/write to a database
  - login failures
  - requests for missing resources
  - concurrent modifications
  - validation errors
- TRACE should be used for normal steps that might be of interest, like
  - calling functions
