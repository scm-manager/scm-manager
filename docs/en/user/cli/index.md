---
title: CLI Client
partiallyActive: true
---

SCM Manager provides a CLI client to directly access e.g. repositories, users and group of the SCM server inside your terminal.

# Installation / Setup
The CLI Client is available for different operating systems and architectures on the official website for [download](https://scm-manager.org/cli/).
Choose the appropriate installation for you and follow the installation instructions.

## Log in
In order to use the CLI client, it must be connected to the SCM server beforehand.
To do this, run the command `scm login 'https://{server-url:port}/scm'`. Replace your server url and port (if necessary).

### API key
When you log in, an API key is generated on the server, which will be used for all further accesses.
This API key has all rights that the logged in user has on the server.
If the CLI client is no longer to be used with the server, it is sufficient to remove the API key on the server side.

### Local configuration
When the API key is created on the server, the same key is simultaneously stored in encrypted form on the executing system.

# Usage
The commands of the CLI client are always defined by the server. Only `login` and `logout` are always known in the CLI client.
With `scm --help` the existing commands can be displayed on the top level.
Otherwise the CLI client largely documents itself. For this purpose, the `--help` option can be appended at any point.

## Structure
Many commands are nested, with the top level representing the resource, e.g. `repo`, `user` or `group`.
The actions such as `create`, `get` or `list` then follow below.
Besides the command structure the CLI client distinguishes between `parameters` as mandatory arguments and `options` as optional arguments.

# Language
The CLI client is available in several languages. The language setting of the underlying operating system decides which language is used for the output. 
The fallback language is english.

# Logout
If the CLI client is to be logged out of the server, the command `scm logout` is sufficient.
This will delete the API key on the server and locally.
After logging out, `scm login 'https://{server-url:port}/scm'` can be used to log in again.
This process can be used for example to change the CLI client to another user (with different rights).
