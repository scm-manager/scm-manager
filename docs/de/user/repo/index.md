---
title: Repository
partiallyActive: true
---
<!--- AppendLinkContentStart -->
Der Bereich Repository umfasst alles auf Basis von Repositories in Namespaces. Dazu zählen alle Operationen auf Branches, der Code und Einstellungen.

* [Branches](branches/)
* [Tags](tags/)
* [Code](code/)
* [Compare](compare/)
* [Einstellungen](settings/)
<!--- AppendLinkContentEnd -->

### Übersicht
Auf der Übersichtsseite der Repositories werden die einzelnen Repositories nach Namespaces gegliedert aufgelistet.

![Repository Übersicht](assets/repository-overview.png)

Mithilfe der Auswahlbox oben auf der Seite kann die Anzeige der Repositories auf einen Namespace eingeschränkt werden. Alternativ kann die Überschrift eines Namespace angeklickt werden, um nur Repositories aus diesem Namespace anzuzeigen. Über die Suchleiste neben der Auswahlbox können die Repositories frei gefiltert werden. Die Suche filtert dabei nach dem Namespace, dem Namen und der Beschreibung der Repositories.

Zusätzlich können über das Icon rechts neben den Überschriften für die Namespaces weitere Einstellungen auf Namespace-Ebene vorgenommen werden.

### Repository erstellen
Im SCM-Manager können neue Git, Mercurial & Subersion (SVN) Repositories über ein Formular angelegt werden. Dieses kann über den Button "Repository erstellen" aufgerufen werden. Dabei muss ein gültiger Name eingetragen und der Repository-Typ bestimmt werden. 
 
Optional kann man das Repository beim Erstellen direkt initialisieren. Damit werden für Git und Mercurial jeweils der Standard-Branch (master bzw. default) angelegt. Außerdem wird ein initialer Commit ausgeführt, der eine README.md erzeugt. 
Für Subversion Repositories wird die README.md in einen Ordner `trunk` abgelegt.

Ist die Namespace-Strategie auf "Benutzerdefiniert" eingestellt, muss noch ein Namespace eingetragen werden.
Für den Namespace gelten dieselben Regeln wie für den Namen des Repositories. Darüber hinaus darf ein Namespace
nicht nur aus bis zu drei Ziffern (z. B. "123") oder den Wörter "create" und "import" bestehen.
Bei der Eingabe werden nach den ersten Zeichen bereits bestehende passende Werte vorgeschlagen, sodass diese leichter
übernommen werden können. Ein neuer Namespace muss explizit mit dem entsprechenden Eintrag in der Vorschlagsliste
neu erstellt werden.

![Repository erstellen](assets/create-repository.png)

### Repository importieren
Neben dem Erstellen von neuen Repository können auch bestehende Repository in den SCM-Manager importiert werden.
Wechseln Sie über den Schalter oben rechts auf die Importseite und füllen Sie die benötigten Informationen aus.

In Abhängigkeit vom Typen des zu importierenden Repositories gibt es verschiedene Möglichkeiten:
- **Import via URL** (nur Git und Mercurial): Hier wird ein Repository von einem anderen Server über die gegebene URL
  importiert. Zusätzlich kann ein Benutzername und ein Passwort für die Authentifizierung angegeben werden. Für Git
  Repositories kann darüber hinaus der Import von LFS Dateien (falls vorhanden) ausgeschlossen werden.
- **Import aus Dump ohne Metadaten**: Hier kann eine Datei hochgeladen werden. Dieses kann entweder ein einfacher Export
  aus einer anderen SCM-Manager Instanz sein oder ein Dump aus einem anderen Repository:
  - Für Git und Mercurial muss es das per Tar gepackte "interne" Verzeichnis sein (das `.git` bzw. das `.hg` Verzeichnis).
  - Für SVN kann ein per `svnamdin` erstellter Dump genutzt werden.
  Wenn diese Dateien per Zip komprimiert sind, muss die Option "Komprimiert" gewählt werden.
- **Import aus SCM-Manager-Dump mit Metadaten**: Mit dieser Option können Exporte mit Metadaten aus anderen SCM-Manager
  Instanzen importiert werden. Dieses Repository Archiv wird vor dem Import auf
  Kompatibilität der Daten überprüft (der SCM-Manager und alle installierten Plugins müssen mindestens die Version des
  exportierenden Systems haben).

Ist die zu importierende Datei verschlüsselt, muss das korrekte Passwort zum Entschlüsseln mitgeliefert werden.
Wird kein Passwort gesetzt, geht der SCM-Manager davon aus, dass die Datei unverschlüsselt ist.

Das gewählte Repository wird zum SCM-Manager hinzugefügt und sämtliche Repository Daten inklusive aller Branches und Tags werden importiert.


![Repository importieren](assets/import-repository.png)

### Repositoryinformationen
Die Informationsseite eines Repositorys zeigt die Metadaten zum Repository an. Darunter befinden sich Beschreibungen zu den unterschiedlichen Möglichkeiten wie man mit diesem Repository arbeiten kann. 
In der Überschrift kann der Namespace angeklickt werden, um alle Repositories aus diesem Namespace anzuzeigen.

![Repository-Information](assets/repository-information.png)
