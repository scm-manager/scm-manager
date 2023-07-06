---
title: Administration
subtitle: Plugins
---
In the plugins section, plugins for SCM-Manager can be managed with the help of the external plugin center. Plugins are distinguished between installed and available plugins. They are grouped based on their main functionality like for example workflow or authentication.

Plugins can be managed by action icons on the tiles. System relevant plugins that come with SCM-Manager by default cannot be uninstalled or updated.

In order for changes to plugins to become effective, the SCM-Manager server needs to be restarted. That can be done after every single action. It is also possible to queue several actions like the installation of a new plugin, updates or the deletion of a plugin and to perform all actions with one restart. If an action (installation, uninstallation, update) for a plugin was performed, the buttons "Execute changes" and "Abort changes"  appear. If you choose to execute  the changes, a popup window that shows the current queue (all actions without a restart) appears. Now the user can decide whether to execute the changes by restarting the server. If there are actions in the queue that are no longer desired, the queue can be emptied with the abort changes button.

### cloudogu platform plugins
Some special plugins are only available to instances of SCM-Manager that are connected to the cloudogu platform. You may connect your instance by clicking the button “Connect to cloudogu platform”.
[More details on data processing.](https://scm-manager.org/data-processing)

![Plugin-center not connected](assets/administration-plugin-center-not-connected.png)
You will be redirected to a cloudogu platform login form. 
![cloudogu platform-Login-Form](assets/cloudogu-platform-login.png)
If you already have an account you simply log in. Otherwise you can create an account either by using a confederate identity provider (Google or github) or with your email.
After a successful login you will return to the SCM-Manager. Here you can review the instance and account to connect. By clicking the button “Connect” you approve the connection and return to the plugin center. 
![Confirmation of connection](assets/administration-cloudogu-platform-confirmation.png)
Now you can install cloudogu platform plugins like basic plugins.
![Plugin-center connected with the cloudogu platform](assets/administration-plugin-center-connected.png)
Only one user with sufficient permissions needs to connect the instance with the cloudogu platform. The cloudogu platform plugins can than be installed by every user with suitable permissions.
You can always sever the connection in the plugin center settings in global settings of your instance.

#### What is the cloudogu platform and why should you create an account?
The cloudogu platform is not only the home of the SCM-Manager community. You can connect to other users, get help and express feature requests in the forum. 
The cloudogu platform also serves special plugins to provide more value for our community. In the future the cloudogu platform will offer exiting plugins developed in cooperation with our partners.
To unlock the full power of SCM-Manager and to hang out with our developers, join the [cloudogu platform](https://platform.cloudogu.com/) for free! 

### Installed
The overview for installed plugins shows all plugins that are currently installed on the SCM-Manager instance. Optional plugins can be uninstalled or updated here.

![Administration-Plugins-Installed](assets/administration-plugins-installed.png)

### Available
The overview of all available plugins shows all plugins that are compatible with the current version of the SCM-Manager instance that are available through the SCM-plugin-center. The plugins can be downloaded by clicking on the icon and will be installed after a restart of the SCM-Manager server.
Special cloudogu platform-plugins can be installed the same way if your instance of SCM-Manager is connected to the cloudogu platform as described above.

![Administration-Plugins-Available](assets/administration-plugins-available.png)

### Manual Installation
Plugins are packaged as `smp` files. To install a plugin using such a file, simply copy it to the `plugins`
folder in your scm home directory (you should find directories for other plugins there, for example `scm-git-plugin`).
The server will find these files on startup and finish the installation. Keep in mind though, that manual installation
might have some risks, because the server cannot check for missing dependencies to other plugins or other requirements
beforehand, and therefore the startup may fail. So keep an eye on your log if you choose this way.

To remove a plugin, simply delete the directory created by the server for this plugin. Here, too, one has to be
cautious, because other plugins may depend on this plugin. The so-called core plugins (git, mercurial, svn und legacy)
cannot be deleted.
