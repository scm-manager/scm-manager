---
title: Repository
subtitle: Einstellungen
---
Unter den Repository Einstellungen befinden sich zwei Einträge. Wenn weitere Plugins installiert sind, können es deutlich mehr Unterseiten sein.

### Generell
Unter dem Eintrag "Generell" kann man die Zusatzinformationen zum Repository editieren. Da es sich im Beispiel um ein Git Repository handelt, kann ebenfalls der Standard-Branch für dieses Repository gesetzt werden. Der Standard-Branch sorgt dafür, dass beim Arbeiten mit diesem Repository dieser Branch vorrangig geöffnet wird, falls kein expliziter Branch ausgewählt wurde.

Innerhalb der Gefahrenzone unten auf der Seite gibt es mit entsprechenden Rechten die Möglichkeit das Repository umzubenennen oder zu löschen. Wenn in der globalen SCM-Manager Konfiguration die Namespace Strategie `benutzerdefiniert` ausgewählt ist, kann zusätzlich zum Repository Namen auch der Namespace umbenannt werden.

![Repository-Settings-General-Git](assets/repository-settings-general-git.png)

### Berechtigungen
Dank des fein granularen Berechtigungskonzepts des SCM-Managers können Nutzern und Gruppen, basierend auf definierbaren Rollen oder auf individuellen Einstellungen, Rechte zugewiesen werden. Berechtigungen können global und auf Repository-Ebene vergeben werden. Globale Berechtigungen werden in der Administrations-Oberfläche des SCM-Managers vergeben. Unter diesem Eintrag handelt es sich um Repository-bezogene Berechtigungen. 

Die Berechtigungen können jeweils für Gruppen und für Benutzer vergeben werden. Dabei gibt es die Möglichkeiten die Berechtigungen über Berechtigungsrollen zu definieren oder jede Berechtigung einzeln zu vergeben. Die Berechtigungsrollen können in der Administrations-Oberfläche definiert werden.

![Repository-Settings-PermissionOverview](assets/repository-settings-permissionOverview.png)

Für individuelle Berechtigungen kann man über "Erweitert" einen Dialog öffnen, um jede Berechtigung einzeln zu vergeben.

![Repository-Settings-PermissionList](assets/repository-settings-permissionList.png)
