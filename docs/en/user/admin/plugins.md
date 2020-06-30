---
title: Administration
subtitle: Plugins
---
In the plugins section, plugins for SCM-Manager can be managed with the help of the external plugin center. Plugins are distinguished between installed and available plugins and are grouped based on their main functionality like for example workflow or authentication.

Plugins can be managed by functionality icons on the tiles. System relevant plugins that come with SCM-Manager by default cannot be deinstalled or updated.

In order for changes to plugins to become effective, the SCM-Manager server needs to be restarted. That can be done after every single action. It is also possible to queue several actions like the installation of a new plugin, updates or the deletion of a plugin and to perform all actions with one restart. If an action (installation, deinstallation, update) for a plugin was performed, the buttons "Execute changes" and "Abort changes"  appear. If you choose to execute  the changes, a popup window that shows the current queue (all actions without a restart) appears. Now the user can decide whether to execute the changes by restarting the server. If there are actions in the queue that are no longer desired, the queue can be emptied with the about changes  button.

### Installed
The overview for installed plugins shows all plugins that are currently installed on the SCM-Manager instance. Plugins that are optional can be deinstalled or updated here.

![Administration-Plugins-Installed](assets/administration-plugins-installed.png)

### Available
The overview of all available plugins shows all plugins that are compatible with the current version of the SCM-Manager instance that are available through the SCM-plugin-center. The plugins can be downloaded by clicking on the icon and will be installed after a restart of the SCM-Manager server.

![Administration-Plugins-Available](assets/administration-plugins-available.png)
