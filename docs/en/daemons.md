# Unix Daemons and Windows Services

**Note**: If you are using a 64Bit operating system you should use a 64Bit JavaVirtualMachine as well 
([#74](https://github.com/scm-manager/scm-manager/issues/74) or 
[rOL1nJ9DnfI](https://groups.google.com/forum/?fromgroups#!topic/scmmanager/rOL1nJ9DnfI "Can't start scm windows service")).

### Unix Daemons

You could run scm-server in background as unix daemon with one simple command:

```bash
scm-server start
```

If you would like to stop the running daemon instance just call:

```bash
scm-server stop
```

### Windows Services

Register scm-server as Windows service open a console (cmd) as Administrator and execute the following command: 

```bash
scm-server.bat install
```

The service is no available in the service control center. You could uninstall the service with the command below:

```bash
scm-server.bat uninstall
```
