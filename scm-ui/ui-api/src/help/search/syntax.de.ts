/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
export default `## Modifikatoren

Hinweis: Sie können keine Wildcards als erstes Zeichen einer Suche verwenden.

<table>
  <tr>
    <th>Definition</th>
    <th>Beispiel</th>
  </tr>
  <tr>
    <td>? - Einzelzeichen-Wildcard</td>
    <td>Ultimate?Repo - findet z.B. Ultimate-Repo, Ultimate Repo, Ultimate+Repo</td>
  </tr>
  <tr>
    <td>* - mehrstelliger Platzhalter</td>
    <td>Ultimat*y - findet z.B. Ultimate Repository, Ultimates-Spezial-Repository, Ultimate</td>
  </tr>
</table>


### Bereiche

Bereichsabfragen ermöglichen den Abgleich von Dokumenten, deren Feldwerte zwischen der unteren und der oberen Grenze liegen, die in der Bereichsabfrage angegeben sind. Bereichsabfragen können die obere und untere Grenze einschließen oder ausschließen. Die Sortierung erfolgt lexikografisch.

Bereiche sind nicht auf numerische Felder beschränkt. 

<table>
  <tr>
    <th>Definition</th>
    <th>Beispiel</th>
  </tr>
  <tr>
    <td>[ … TO … ] - inklusiver Bereich</td>
    <td>creationDate:[1609459200000 TO 1612137600000] – findet z.B. Repositories, die zwischen dem 01.01.2021 und dem 01.02.2021 angelegt wurden.</td>
  </tr>
  <tr>
    <td>{… TO …} - ausschließender Bereich</td>
    <td>name:{Aida TO Carmen} – findet Namen zwischen Aida und Carmen, jedoch ohne die beiden Namen einzuschließen.</td>
  </tr>
</table>


## Boosten

Mit dem Boosting können Sie die Relevanz eines Dokuments steuern, indem Sie seinen Term verstärken.

<table>
  <tr>
    <th>Definition</th>
    <th>Beispiel</th>
  </tr>
  <tr>
    <td>term^Zahl</td>
    <td>ultimate^2 repository – erhöht die Relevanz von „ultimate"</td>
  </tr>
</table>


Standardmäßig ist der Boost-Faktor 1. Obwohl der Boost-Faktor positiv sein muss, kann er kleiner als 1 sein (z. B. 0,2)

Standardmäßig werden Repository-Namen um 1,5 und Namespace-Namen um 1,25 geboostet.

## Boolesche Operatoren

Hinweis: Logische Operatoren müssen in Großbuchstaben eingegeben werden (z. B. „AND").

<table>
  <tr>
    <th>Operator</th>
    <th>Definition</th>
    <th>Beispiel</th>
  </tr>
  <tr>
    <td>AND</td>
    <td>Beide Terme müssen enthalten sein</td>
    <td>Ultimate AND Repository – findet z.B. Ultimate Repository, Ultimate Special Repository</td>
  </tr>
  <tr>
    <td>OR</td>
    <td>Mindestens einer der Terme muss enthalten sein</td>
    <td>Ultimate OR Repository – findet z.B.. Ultimate Repository, Ultimate User, Special Repository</td>
  </tr>
  <tr>
    <td>NOT</td>
    <td>Der nachfolgende Term darf nicht enthalten sein, „!" kann alternativ verwendet werden</td>
    <td>Ultimate NOT Repository – findet z.B.. Ultimate user, nicht jedoch z.B. Ultimate Repository</td>
  </tr>
  <tr>
    <td>–</td>
    <td>Schließt den folgenden Term von der Suche aus</td>
    <td>Ultimate Repository -Special – findet z.B. Ultimate Repository, schließt z.B. Ultimate Special Repository aus</td>
  </tr>
  <tr>
    <td>+</td>
    <td>Der folgende Term muss enthalten sein</td>
    <td>Ultimate +Repository – findet z.B. my Repository, Ultimate Repository</td>
  </tr>
</table>


## Gruppieren

Die Suche unterstützt die Verwendung von Klammern zur Gruppierung von Begriffen, um Teilabfragen zu bilden. Dies kann sehr nützlich sein, wenn Sie die boolesche Logik für eine Abfrage steuern möchten.

<table>
  <tr>
    <th>Definition</th>
    <th>Beispiel</th>
  </tr>
  <tr>
    <td>() – Terme zwischen den Klammern werden gruppiert</td>
    <td>(Ultimate OR my) AND Repository – findet z.B.. Ultimate Repository, my Repository, schließt z.B. Super Repository. Entweder "Ultimate" oder “My” müssen im Ergebnis existieren, “Repository” muss immer enthalten sein.
 </td>
  </tr>
</table>


## Umgang mit Sonderzeichen

Die Suche unterstützt Sonderzeichen, die Teil der Abfragesyntax sind. Die aktuellen Sonderzeichen der Liste sind

&plus; &minus; && || ! ( ) { } [ ] ^ " ~ * ? : &bsol; /

Um diese Zeichen zu nutzen, verwenden Sie „&bsol;" vor dem jeweiligen Zeichen. Um zum Beispiel nach (1+1):2 zu suchen, verwenden Sie diese Abfrage:

&bsol;(1&bsol;+1&bsol;)&bsol;:2

Partiell übersetzt mit www.DeepL.com/Translator (kostenlose Version)

Quelle [https://javadoc.io/static/org.apache.lucene/lucene-queryparser/8.9.0/org/apache/lucene/queryparser/classic/package-summary.html#package.description](https://javadoc.io/static/org.apache.lucene/lucene-queryparser/8.9.0/org/apache/lucene/queryparser/classic/package-summary.html#package.description)`;
