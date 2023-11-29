---
title: Error Handling
---

As a highly extensible product, SCM-Manager offers at least three ways to interact with:

- the GUI,
- the REST API, and
- the Java API

Having these three layers, the error handling should be consistent among these. That is, as a developer I would not like
to have custom made error codes in the REST layer that I cannot find in the Java API. Furthermore it is essential to get
precise error messages with hints how to find a way out (if possible), not only for a programmer making interactive
calls, but also for other programs. Last but not least it should be easy for plugin developers to adapt the error
handling.

On the GUI layer, these information have to be translated into messages that can be easily handled.

Here are some example error cases:

*Use case*: Read the metadata for a branch

*Possible errors:*

- The repository is missing
- There is no such branch
- The user is not authorized to read the metadata
- The repository is corrupt on file system level

 ---

*Use case:* Create a new user

*Possible errors:*

- Invalid characters in name
- Missing mandatory property
- Conflict with an existing user
- Insufficient priviliges

---

*Use case:* Update the e-mail of a repository

*Possible errors:*

- The repository does not exist
- The repository was modified concurrently
- Invalid e-mail address

## Java API

In SCM-Manager we make heavy use of Java `Exception`s, not only for technical exceptions in the program flow like
reading corrupt file systems, but also for "user errors" like illegal values or requests for missing data.

These exceptions are handled by
JEE [`ExceptionMapper`](https://docs.oracle.com/javaee/7/api/jakarta/ws/rs/ext/ExceptionMapper.html) s. Doing so, it is
possible to concentrate on implementing the "happy path" without the need to explicitly handle error cases everywhere (
for example you do not have to check whether got `null` as a result). Nonetheless we still had to decide whether to use
checked or unchecked exceptions. We have chosen to use unchecked exceptions due to the following reasons:

- Checked exceptions would have had to be declared everywhere.
- A checked exception can somehow trigger a "I have to handle this though I don't know how" feeling that would be wrong,
  because we do have mappers for these exceptions.

Therefore handling such an exception has to be a concious decision.

In the following we will introduce the exceptions we are using:

### Used exceptions

#### `NotFoundException`

A `NotFoundException` is thrown, whenever "things" where requested but were not found.

#### `AlreadyExistsException`

This exception is thrown whenever an entity cannot be created, because another entity with the same key identifyer
exists.

#### `ConcurrentModificationException`

When you try to modify an entity based on an outdated version, this exception is thrown. For entities like user, group
or repository the "last modified" timestamp of `ModelObject` is used to check this.

#### `NativeRepositoryAccessException`

Failures while accessing native repositories (most of the time) result in `java.io.IOException`s. To distinguish these
exceptions from other I/O errors like network exceptions and to make them unchecked, we wrap them
in `NativeRepositoryAccessException`.

#### `ResteasyViolationException`

Input validation is handled
using [RESTEasy's validation support](https://docs.jboss.org/resteasy/docs/3.0.0.Final/userguide/html/Validation.html).
Constraint violations result in `ResteasyViolationException`s.

#### All other runtime exceptions

All other `java.lang.RuntimeException`s can be treated as unexpected errors, that either hint to severe problems (eg.
disk access failures) or implementation errors. It is unlikely that these can be handled by the program gracefully. They
will be caught by a generic exception handler which will wrap them in a new exception providing further SCM specific
information.

#### Checked exceptions

Above we mentioned, that we want to use unchecked exceptions only. Therefore we have to wrap checked exceptions for
example created by libraries or frarmeworks to make them unchecked. Normally it is sufficiant to wrap them in a
RuntimeException using a proper message, except you plan to handle them somewhere else than at resource level (then it
would be appropriate to introduce a new exception class extending `RuntimeException`).

### Enrichment of exceptions

#### Context

Most of these exceptions must provide information about _what_ could not have been found, updated, whatsoever. This is
necessary, because otherwise it may not be clear at what step of a potentionally complex process this exception occured.
Though this sounds easy, it has some complexity because for example the access of a file in a repository can fail on
many levels (the file is missing in the given revision, the revision is missing, or the repository itself is missing).
In these cases you have to know the complete access path (what file in what revision of what changeset in what
repository).

To ensure that this is done in a reproducable and consistent way, SCM-Manager will provide utility functions to creaate
such exceptions.

#### Transaction IDs

To be able to retrace the cause for exceptions, it is helpful to link log messages to these exceptions. To do so,
SCM-Manager introduces transaction ids that are generated for single requests or other related actions like the
processing of hooks or cron jobs. This transaction id will be part of every log message and every API response.

For http requests, this can be done using MDC filter.

#### Identification of exceptions

To be able to identify different types of exceptions even outside of the java ecosystem, each SCM-Manager exception
class will get a unique type id that will be created using the `DefaultKeyGenerator` during development. We chose to
generate these ids and not "human readable" ones to prevent collisions between plugins.

### Logging of exceptions

To be able to retrace errors a proper logging is indispensible. So we decided to use the following rules for logging
exceptions:

- Native SCM manager exceptions will be logged at log level `INFO` with their message, only. The complete stacktrace
  will be logged at log level `DEBUG`.
- All other exceptions will be logged on level `WARN` with the complete stacktrace, because they indicate something
  going wrong on a fundamental level.

## REST API

### Status codes

SCM-Manager uses [http status codes](https://en.wikipedia.org/wiki/List_of_HTTP_status_codes) to identify types of
errors (and successes, that is) and doing so provides a first hint, what may have gone wrong:

| Status code | Principal error cause |
|-------------|-----------------------|
| 200 | No error, everything is fine |
| 201 | The item has been created without an error |
| 204 | The request has been processed without an error |
| 400 | Something is not valid with the data provided |
| 401 | Missing authentication (not logged in?) |
| 403 | Missing authorization |
| 404 | The thing you are looking does not exist |
| 409 | Your update was rejected because you relate to an outdated version (maybe this item was changed in the meantime) _
or_ the item could not be created because the key already exists |
| 500 | The "You are not to blame" error; something unexpected went wrong while processing the request |

### Further information

Whenever possible, an error response contains useful details about the error in a simple json format. These information
are _not_ translated, so this is the responsibility of the frontend.

| key | content | availability |
|-----|---------|--------------|
| transactionId | A unique id to link your request to log messages | always |
| errorCode | A code that can be used for translated error messages. To prevent the usage of the same codes in different exceptions we decided to use generated ids. | always |
| context | (repo/key, branch/key, ...) | optional |
| message | An english error message (not necessarily for end users) | always |
| url | A URL to a site providing further information about the error | optional |

Error objects will contain no stack traces.

For SCM exceptions, the message will be created from the message of the java exception. For other exceptions this will
be a generic message in most cases.

Here is an example, how a concrete exception may look like in a json response:

```json
{
  "transactionId": "7D82atGf3",
  "errorCode": "H823fFAt",
  "context": [
    {
      "type": "repository",
      "id": "scmmanager/test"
    },
    {
      "type": "branch",
      "id": "master"
    },
    {
      "type": "file",
      "id": ".gitignore"
    }
  ],
  "message": "file not found",
  "url": "https://www.scm-manager.org/errors/H823fFAt"
}
```

### Missing resources (404)

The http status code 404 is a special case, because it is a fundamental status code that can be created on a lot of
events:

- Your proxy has a misconfiguration and you are talking with a static website instead of SCM-Manager
- You are using a path without a valid endpoint in SCM-Manager
- You are requesting a entity that does not exists

Some say, that you should not try to interpret the body of a 404 response, because the origin of the response cannot be
taken for granted. Nonetheless we decided to use this http status code to indicate requests for missing resources,
because in our view this is what most people would expect.

### Internal errors (500)

Internal errors boil down to the following message: An error occured, that could not be handled in a reasonable way by
the program. In these cases often only an administrator can help. Examples are out-of-memory errors, failing disk I/O,
timeouts accessing other services, or (to be honest) simple programming errors that have to be fixed in further
releases. To be able to trace these errors in the logs one can use the transaction ids.

## GUI

As an end user of the SCM-Manager I would not like to see confusing internals, but rather have a meaningful message in
my language of choice. Therefore it is necessary to identify error types on a fine level. This can be done using the
errorCode provided in each error object.

Basically we have to differentiate between errors the user can handle ("user errors") and technical exceptions. For user
errors a meaningful message can be generated giving hints to what the user has done "wrong". All other exceptions can be
handled by displaying a "sorry, this did not work as expected" message with the transaction id.

## Resources / Best Practices

While creating this concepts we tried to adhere to best practices considering APIs of Twitter, Facebook, Bing, Spotify
and others, as summarized in the following articles:

* [RESTful API Design: What About Errors? (Apigee)](https://apigee.com/about/blog/technology/restful-api-design-what-about-errors)
* [Best Practices for API Error Handling (Nordic APIS)](https://nordicapis.com/best-practices-api-error-handling/)
