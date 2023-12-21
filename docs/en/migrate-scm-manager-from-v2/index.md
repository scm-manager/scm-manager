---
title: Migrate from v2 to v3
subtitle: Changes in the configuration
---

# Configuration changes

The configuration of SCM-Manager version 3 has changed. While in version 2 the configuration was split into different
files, environment variables and Java properties, the configuration is now stored in a single file `config.yml` in the
`conf` directory of the SCM installation folder. The file is in [YAML](https://yaml.org/). Our goal was to make the
configuration more readable and easier to understand.

If needed, all these settings now can be overwritten by environment variables. This makes it easier to configure
SCM-Manager in a Docker container or Kubernetes.

So if you have set up your SCM-Manager v2 with a custom configuration using different ports, SSL, specific logging and
so on, you have to migrate your configuration to the new format. Please see
the [configuration documentation](../administration/scm-server) for more information.
