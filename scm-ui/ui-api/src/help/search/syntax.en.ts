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
export default `### Modifiers

Note: You can not use wildcards as the first character of a search.

<table>
  <tr>
    <th>Definition</th>
    <th>Example</th>
  </tr>
  <tr>
    <td>? - single character Wildcard</td>
    <td>Ultimate?Repo – finds e.g. Ultimate-Repo, Ultimate Repo, Ultimate+Repo</td>
  </tr>
  <tr>
    <td>* - multiple character Wildcard</td>
    <td>Ultimat*y - finds e.g. Ultimate Repository, Ultimate-Special-Repository, Ultimately</td>
  </tr>
</table>


### Ranges

Range Queries allow one to match documents whose field(s) values are between the lower and upper bound specified by the Range Query. Range Queries can be inclusive or exclusive of the upper and lower bounds. Sorting is done lexicographically.

Ranges are not reserved to numerical fields. 

<table>
  <tr>
    <th>Definition</th>
    <th>Example</th>
  </tr>
  <tr>
    <td>[ … TO … ] - inclusive range</td>
    <td>creationDate:[1609459200000 TO 1612137600000] – finds e.G. repositories created between 2021-01-01 and 2021-02-01 </td>
  </tr>
  <tr>
    <td>{… TO …} - exclusive range</td>
    <td>name:{Aida TO Carmen} – finds e.G. repositories with names between Aida and Carmen, excluding these to values.</td>
  </tr>
</table>


### Boosting

Boosting allows you to control the relevance of a document by boosting its term. 

<table>
  <tr>
    <th>Definition</th>
    <th>Example</th>
  </tr>
  <tr>
    <td>term^number</td>
    <td>ultimate^2 repository – makes the term "ultimate" more relevant. </td>
  </tr>
</table>


By default, the boost factor is 1. Although the boost factor must be positive, it can be less than 1 (e.g. 0.2)

By default Repository names are boosted by 1.5, namespace by 1.25.

## Boolean Operators

Note: Logical Operators must be entered in upper case (e.g. "AND").

<table>
  <tr>
    <th>Definition</th>
    <th>Example</th>
  </tr>
  <tr>
    <td>AND – both terms must be included</td>
    <td>Ultimate AND Repository – finds e.g. Ultimate Repository, Ultimate Special Repository</td>
  </tr>
  <tr>
    <td>OR – at least one of the terms must be included</td>
    <td>Ultimate OR Repository – finds e.g. Ultimate Repository, Ultimate User, Special Repository</td>
  </tr>
  <tr>
    <td>NOT – following term may not be included, "!" may be used alternatively</td>
    <td>Ultimate NOT Repository – finds e.g. Ultimate user, excludes e.g. Ultimate Repository</td>
  </tr>
  <tr>
    <td>– excludes following term from search</td>
    <td>Ultimate Repository -Special – finds e.g. Ultimate Repository, excludes e.g. Ultimate Special Repository</td>
  </tr>
  <tr>
    <td>– following term must be included</td>
    <td>Ultimate +Repository – finds e.g. my Repository, Ultimate Repository</td>
  </tr>
</table>


## Grouping

Search supports using parentheses to group clauses to form sub queries. This can be very useful if you want to control the boolean logic for a query.

<table>
  <tr>
    <th>Definition</th>
    <th>Example</th>
  </tr>
  <tr>
    <td>() – terms inside parentheses are grouped together</td>
    <td>(Ultimate OR my) AND Repository – finds e.g. Ultimate Repository, my Repository, excludes e.g. Super Repository. Either "Ultimate" or “My” must exist, “Repository” must always exist.
 </td>
  </tr>
</table>


## Escaping Special Characters

The search supports escaping special characters that are part of the query syntax. The current list special characters are

&plus; &minus; && || ! ( ) { } [ ] ^ " ~ * ? : &bsol; /

To escape these characters use the "&bsol;" before the character. For example to search for (1+1):2 use the query:

&bsol;(1&bsol;+1&bsol;)&bsol;:2

Source: [https://javadoc.io/static/org.apache.lucene/lucene-queryparser/8.9.0/org/apache/lucene/queryparser/classic/package-summary.html#package.description](https://javadoc.io/static/org.apache.lucene/lucene-queryparser/8.9.0/org/apache/lucene/queryparser/classic/package-summary.html#package.description)`;
