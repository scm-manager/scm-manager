---
title: CLI Client
partiallyActive: true
---

SCM-Manager bietet einen CLI Client an, um direkt in der Konsole auf z. B. Repositories, Benutzer und Gruppe des SCM-Servers zuzugreifen. 

# Installation / Einrichtung
Der CLI Client steht für unterschiedliche Betriebssysteme und Architekturen auf der offiziellen Webseite zum [Download](https://scm-manager.org/cli/) bereit.
Wählen Sie die passende Installation für sich aus und folgen Sie der Installationsanleitung.

## Anmelden
Um den CLI Client zu nutzen, muss dieser vorher mit dem SCM-Server verbunden werden. 
Dazu führen Sie den Befehl `scm login 'https://{server-url:port}/scm'` aus. Ersetzen Sie dabei Ihre Server URL und den Port (falls vorhanden).

### API Schlüssel
Beim Login wird ein API Schlüssel auf dem Server erzeugt, der für alle weiteren Zugriffe verwendet wird. 
Dieser API Schlüssel verfügt über alle Rechte, die der angemeldete Benutzer auf dem Server hat. 
Soll der CLI Client nicht mehr mit dem Server verwendet werden dürfen, reicht es diesen Schlüssel serverseitig zu entfernen.

### Lokale Konfiguration
Beim Anlegen des API Schlüssels auf dem Server wird zeitgleich derselbe Schlüssel in verschlüsselter Form auf dem ausführenden System hinterlegt.

# Nutzung
Die Befehle des CLI Clients werden immer vom Server definiert. Einzig `login` und `logout` sind im CLI Client immer bekannt.
Mit `scm --help` können die existierenden Befehle auf der obersten Ebene angezeigt werden.
Ansonsten dokumentiert sich der CLI Client weitgehend selbst. Dazu kann an jeder Stelle die Option `--help` angehangen werden.

## Struktur
Viele Befehle sind geschachtelt, wobei die oberste Ebene die Resource abbildet, z. B. `repo`, `user` oder `group`.
Darunter folgen dann die Aktionen wie `create`, `get` oder `list`.
Neben der Befehlsstruktur unterscheidet der CLI Client noch zwischen `Parametern` als Pflichtargumente und `Optionen` als optionale Argumente.

# Sprache
Der CLI Client steht in mehreren Sprachen zu Verfügung. Dabei entscheidet die Spracheinstellung des unterliegenden Betriebssystems welche Sprache für die Ausgaben verwendet wird.

# Abmelden
Soll der CLI Client vom Server abgemeldet werden, reicht der Befehl `scm logout`. 
Dabei werden der API Schlüssel auf dem Server und auch lokal gelöscht.
Nach dem Abmelden kann über `scm login 'https://{server-url:port}/scm'` wieder eine Anmeldung erfolgen.
Dieser Vorgang kann zum Beispiel genutzt werden, um den CLI Client auf einen anderen Benutzer (mit anderen Rechten) zu wechseln.
