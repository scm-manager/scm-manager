---
title: Administration
subtitle: Plugins
---
Unter dem Eintrag "Plugins" können mithilfe des externen Plugin-Centers Plugins für den SCM Manager verwaltet werden. Die Plugins werden nach installierten und verfügbaren Plugins unterschieden und nach Funktionsschwerpunkt wie bspw. Workflow oder Authentifizierung gruppiert.

Die Plugins können über Funktions-Icons auf den Kacheln verwaltet werden. Systemrelevante Plugins, die der SCM-Manager selbst liefert, können weder deinstalliert noch aktualisiert werden.

Damit Änderungen der Plugins wirksam werden, muss der SCM-Manager-Server neugestartet werden. Das kann nach jeder einzelnen Aktion erfolgen. Es ist aber auch möglich viele unterschiedliche Aktionen wie Installieren, Aktualisieren und Löschen in eine Warteschlange einzureihen und alle Aktionen mit einem einzigen Neustart auszuführen. Wird eine Aktion (Installieren, Deinstallieren, Aktualisieren) für ein Plugin ausgewählt, erscheinen die Schaltflächen "Änderungen ausführen" und "Änderungen abbrechen". Über "Änderungen ausführen" öffnet sich ein Pop-Up Fenster, indem die aktuelle Warteschlange (alle ausgeführten Aktionen ohne Neustart) angezeigt werden. Der Anwender hat nun die Möglichkeit zu entscheiden, ob die Änderungen durch einen Neustart ausgeführt werden sollen. Falls Aktionen, die sich bereits in der Warteschlange befinden nicht mehr erwünscht sind, kann die gesamte Warteschlange über den Button "Änderungen abbrechen" verworfen werden.

### Installiert
Auf der Übersicht für installierte Plugins werden alle auf der SCM-Manager Instanz installierten Plugins angezeigt. Optionale Plugins können hier deinstalliert und aktualisiert werden. 

![Administration-Plugins-Installed](assets/administration-plugins-installed.png)

### Verfügbar
Auf der Übersicht der verfügbaren Plugins werden alle kompatiblen Plugins, die über das SCM-Plugin-Center zur Verfügung stehen, aufgeführt. Die Plugins können über den Download-Icon heruntergeladen und mit einem Neustart des SCM-Manager-Servers installiert werden. 

![Administration-Plugins-Available](assets/administration-plugins-available.png)
