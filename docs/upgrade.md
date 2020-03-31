# Upgrade to a new Version

If you would like to update SCM-Manager, you have to do the following
steps.

- Update all plugins: Goto the plugins panel and click every update link

The next steps depends on the version you use.

## Upgrade the Standalone version

- Stop the old instance
- Rename your old version
- Download the latest version of the scm-server bundle
- Extract the new version
- If you have changed bin/scm-server or one of the files from the conf
  directory, you have to make the same changes for the new version
- If you use a windows service you have to reinstall the service
  (uninstallService and installService)

## Upgrade the WebArchive (war) version

- Download the latest version of the war bundle
- Deploy the new version
- If you have changed the WEB-INF/scm.properties or the
  WEB-INF/classes/logback.xml, you have to make the same changes for
  the new version. After you have done the changes you have to restart
  your applicationserver.
