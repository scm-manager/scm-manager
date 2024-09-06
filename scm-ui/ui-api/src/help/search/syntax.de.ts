/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

export default `## Modifikatoren

Hinweis: Sie können keine Wildcards als erstes Zeichen einer Suche verwenden.

|Definition|Beispiel|
|----------|--------|
|? - Einzelzeichen-Wildcard|"Ultimate?Repo" - findet z.B. \`Ultimate-Repo\`, \`Ultimate Repo\`, \`Ultimate+Repo\`|
|* - mehrstelliger Platzhalter|"Ultimat*y" - findet z.B. \`Ultimate Repository\`, \`Ultimates-Spezial-Repository\`, \`Ultimate\`|

### Bereiche

Bereichsabfragen ermöglichen den Abgleich von Dokumenten, deren Feldwerte zwischen der unteren und der oberen Grenze liegen, die in der Bereichsabfrage angegeben sind. Bereichsabfragen können die obere und untere Grenze einschließen oder ausschließen. Die Sortierung erfolgt lexikografisch.

Bereiche sind nicht auf numerische Felder beschränkt. 

|Definition|Beispiel|
|----------|--------|
|[ … TO … ] - inklusiver Bereich|"creationDate:[1609459200000 TO 1612137600000]" – findet z.B. Repositories, die zwischen dem 01.01.2021 und dem 01.02.2021 angelegt wurden|
|{… TO …} - ausschließender Bereich|"name:{Aida TO Carmen}" – findet Namen zwischen Aida und Carmen, jedoch ohne die beiden Namen einzuschließen|

## Boosten

Mit dem Boosting können Sie die Relevanz eines Dokuments steuern, indem Sie seinen Term verstärken.

|Definition|Beispiel|
|----------|--------|
|Begriff^Zahl|"ultimate^2 repository" – erhöht die Relevanz von \`ultimate\`|

Standardmäßig ist der Boost-Faktor 1. Obwohl der Boost-Faktor positiv sein muss, kann er kleiner als 1 sein (z. B. 0,2)

Standardmäßig werden Repository-Namen um 1,5 und Namespace-Namen um 1,25 geboostet.

## Boolesche Operatoren

Hinweis: Logische Operatoren müssen in Großbuchstaben eingegeben werden (z. B. „AND").

|Operator|Definition|Beispiel|
|--------|----------|--------|
|AND|Beide Terme müssen enthalten sein|"Ultimate AND Repository" – findet z.B. \`Ultimate Repository\`, \`Ultimate Special Repository\`|
|OR|Mindestens einer der Terme muss enthalten sein|"Ultimate OR Repository" – findet z.B. \`Ultimate Repository\`, \`Ultimate User\`, \`Special Repository\`|
|NOT|Der nachfolgende Term darf nicht enthalten sein, „!" kann alternativ verwendet werden|"Ultimate NOT Repository" – findet z.B. \`Ultimate user\`, nicht jedoch z.B. \`Ultimate Repository\`|
|–|Schließt den folgenden Term von der Suche aus|"Ultimate Repository -Special" – findet z.B. \`Ultimate Repository\`, schließt z.B. \`Ultimate Special Repository\` aus|
|+|Der folgende Term muss enthalten sein|"Ultimate +Repository" – findet z.B. \`my Repository\`, \`Ultimate Repository\`|

## Gruppieren

Die Suche unterstützt die Verwendung von Klammern zur Gruppierung von Begriffen, um Teilabfragen zu bilden. Dies kann sehr nützlich sein, wenn Sie die boolesche Logik für eine Abfrage steuern möchten.

|Definition|Beispiel|
|----------|--------|
|() – Terme zwischen den Klammern werden gruppiert|"(Ultimate OR my) AND Repository" – findet z.B. \`Ultimate Repository\`, \`my Repository\`, schließt z.B. Super Repository. Entweder "Ultimate" oder "My" müssen im Ergebnis existieren, "Repository" muss immer enthalten sein|

## Phrasen

Eine Phrase ist eine Gruppe von Begriffen in einer bestimmten Reihenfolge. Falls man nach einer bestimmten Phrase suchen will, dann kann der \`"\`-Operator genutzt werden.

|Definition|Beispiel|
|----------|--------|
|"" - Begriffe innerhalb der Anführungszeichen werden als Phrase gesucht|"Ultimate Repository" – findet \`Ultimate Repository\` aber nicht \`Repository Ultimate\`, \`Ultimate\` oder \`Repository\`|

`;
