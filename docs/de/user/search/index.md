---

title: Suchsyntax

partiallyActive: true

---

Der Suchbereich enthält alle notwendigen Informationen, um die Suchoption des SCM-Managers zu nutzen (ausgenommen die Plugin-Funktionalität).

### Arten von Suchanfragen

Es gibt zwei Arten von Suchen. Die erste Art ist die einfache Suche. Eine einfache Suche besteht nur aus Suchbegriffen und beinhaltet keine der unten gelisteten Operatoren (z.B. 'AND'). 
Jeder Suchbegriff einer einfachen Suche wird implizit am Ende mit dem \*-Operator erweitert. Dadurch findet man mit dem Suchbegriff 'Repo' auch den Begriff 'Repository'. 
Die zweite Art von Suche ist die Expertensuche. Sobald eine Suchanfrage einen Operator beinhaltet, dann gilt diese Suchanfrage als Expertensuche. 
Die Art von Suche ermöglicht es, komplexe Suchabfragen zu erstellen. Allerdings werden Suchbegriffe hier nicht implizit mit einem \*-Operator erweitert. Dementsprechend muss hier der \*-Operator bei Bedarf explizit gesetzt werden.

## Aufteilug von Wörtern

Der SCM-Manager ist für Code entwickelt und in Code verwendet man oft "CamelCase" Wörter. Um die Suche nach diesen Wörtern
zu erleichtern, werden sie für die Suche in separate Wörter aufgeteilt. Das bedeutet, dass die Suche nach `SomeManager`
auch den Begriff `SomeRepositoryManager` findet. Wenn Sie dies vermeiden möchten, können Sie Ihre Wörter einfach nur
in Kleinbuchstaben eingeben. Im obigen Beispiel findet die Suche nach `somemanager` nur den Begriff `SomeManager`,
aber nicht `SomeRepositoryManager`.

### Modifikatoren

Hinweis: Sie können keine Wildcards als erstes Zeichen einer Suche verwenden.

Definition | Beispiel
---------|----------
? - Wildcard für ein einzelnes Zeichen | "Ultimate?Repo" – findet z.B. `Ultimate-Repo`, `Ultimate Repo`, `Ultimate+Repo`
\* - Wildcard für mehrere Zeichen | "Ultimat*y" - findet z.B. `Ultimate Repository`, `Ultimate-Special-Repository`, `Ultimately`

### Bereiche

Bereichsabfragen ermöglichen den Abgleich von Dokumenten, deren Feldwerte zwischen der unteren und der oberen Grenze liegen, die in der Bereichsabfrage angegeben sind. Bereichsabfragen können die obere und untere Grenze einschließen oder ausschließen. Die Sortierung erfolgt lexikografisch.

Bereiche sind nicht auf numerische Felder beschränkt.

Definition | Beispiel
-----------|------------
\[ … TO … ] - inklusiver Bereich | "creationDate:\[1609459200000 TO 1612137600000]" – findet z.B. Repositories, die zwischen dem 01.01.2021 und dem 01.02.2021 erstellt wurden
{… TO …} - exklusiver Bereich | "name:{Aida TO Carmen}" – f"name:{Aida TO Carmen}" – findet Namen zwischen Aida und Carmen, jedoch ohne die beiden Namen einzuschließen

### Boosting

Mit dem Boosting können Sie die Relevanz eines Dokuments steuern, indem Sie seinen Term verstärken.

Definition | Beispiel
---------|----------
term^number | "ultimate^2 repository" – erhöht die Relevanz von `ultimate`

Standardmäßig ist der Boost-Faktor 1. Obwohl der Boost-Faktor positiv sein muss, kann er kleiner als 1 sein (z. B. 0,2)

Standardmäßig werden Repository-Namen um 1,5 und Namespace-Namen um 1,25 geboostet.

### Boolesche Operatoren

Hinweis: Logische Operatoren müssen in Großbuchstaben eingegeben werden (z. B. „AND").

Operator | Definition                                                                        | Beispiel
---------|-----------------------------------------------------------------------------------|---------
AND | Beide Terme müssen enthalten sein                                                 | "Ultimate AND Repository" – findet z.B. `Ultimate Repository`, `Ultimate Special` `Repository`
OR | Mindestens einer der Terme muss enthalten sein                                    | "Ultimate OR Repository" – findet z.B. `Ultimate` `Repository`, `Ultimate User`, `Special` `Repository`
NOT | Der folgende Term darf nicht enthalten sein, "!" kann alternativ verwendet werden | "Ultimate NOT Repository" – findet z.B. `Ultimate user`, schließt z.B. `Ultimate Repository` aus
\- | schließt den folgenden Term aus der Suche aus                                     | "Ultimate Repository -Special" – findet z.B. `Ultimate Repository`, schließt z.B. `Ultimate Special Repository` aus
\+ | Der folgende Term muss enthalten sein                                             | "Ultimate +Repository" – findet z.B. `mein Repository`, `Ultimate Repository`

### Gruppierung

Die Suche unterstützt die Verwendung von Klammern zur Gruppierung von Begriffen, um Teilabfragen zu bilden. Dies kann sehr nützlich sein, wenn Sie die boolesche Logik für eine Abfrage steuern möchten.

Definition | Beispiel
-----------|-----------
() – Terme zwischen den Klammern werden gruppiert | "(Ultimate OR my) AND Repository" – findet z.B. `Ultimate Repository`, `my Repository`, schließt z.B. Super Repository aus. Entweder "Ultimate" oder "My" muss existieren, "Repository" muss immer existieren

### Phrasen

Eine Phrase ist eine Gruppe von Begriffen in einer bestimmten Reihenfolge. Wenn Sie nach bestimmten Phrasen suchen möchten, können Sie das " Zeichen verwenden.

Bitte beachten Sie, dass Wörter getrennt werden, wenn sie "CamelCase" verwenden (wie im Abschnitt "Aufteilug von Wörtern" erwähnt), auch wenn sie mit `"` umschlossen sind.

Definition | Beispiel
-----------|-----------
"" - Begriffe innerhalb der Anführungszeichen werden als Phrase gesucht | "Ultimate Repository" – findet `Ultimate Repository`, aber nicht `Repository Ultimate`, `Ultimate` oder `Repository`
