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

export default `### Modifiers

Note: You can not use wildcards as the first character of a search.

|Definition|Example|
|----------|-------|
|? - single character Wildcard|"Ultimate?Repo" – finds e.g. \`Ultimate-Repo\`, \`Ultimate Repo\`, \`Ultimate+Repo\`|
|* - multiple character Wildcard|"Ultimat*y" - finds e.g. \`Ultimate Repository\`, \`Ultimate-Special-Repository\`, \`Ultimately\`|

### Ranges

Range Queries allow one to match documents whose field(s) values are between the lower and upper bound specified by the Range Query. Range Queries can be inclusive or exclusive of the upper and lower bounds. Sorting is done lexicographically.

Ranges are not reserved to numerical fields. 

|Definition|Example|
|----------|-------|
|[ … TO … ] - inclusive range|"creationDate:[1609459200000 TO 1612137600000]" – finds e.G. repositories created between 2021-01-01 and 2021-02-01|
|{… TO …} - exclusive range|"name:{Aida TO Carmen}" – finds e.G. repositories with names between Aida and Carmen, excluding these to values|

### Boosting

Boosting allows you to control the relevance of a document by boosting its term. 

|Definition|Example|
|----------|-------|
|term^number|"ultimate^2 repository" – makes the term \`ultimate\` more relevant|

By default, the boost factor is 1. Although the boost factor must be positive, it can be less than 1 (e.g. 0.2)

By default Repository names are boosted by 1.5, namespace by 1.25.

## Boolean Operators

Note: Logical Operators must be entered in upper case (e.g. "AND").

|Operator|Definition|Example|
|--------|----------|-------|
|AND|Both terms must be included|"Ultimate AND Repository" – finds e.g. \`Ultimate Repository\`, \`Ultimate Special Repository\`|
|OR|At least one of the terms must be included|"Ultimate OR Repository" – finds e.g. \`Ultimate Repository\`, \`Ultimate User\`, \`Special Repository\`|
|NOT|Following term may not be included, "!" may be used alternatively|"Ultimate NOT Repository" – finds e.g. \`Ultimate user\`, excludes e.g. \`Ultimate Repository\`|
|–|Excludes following term from search|"Ultimate Repository -Special" – finds e.g. \`Ultimate Repository\`, excludes e.g. \`Ultimate Special Repository\`|
|+|Following term must be included|"Ultimate +Repository" – finds e.g. \`my Repository\`, \`Ultimate Repository\`|

## Grouping

Search supports using parentheses to group clauses to form sub queries. This can be very useful if you want to control the boolean logic for a query.

|Definition|Example|
|----------|-------|
|() – terms inside parentheses are grouped together|"(Ultimate OR my) AND Repository" – finds e.g. \`Ultimate Repository\`, \`my Repository\`, excludes e.g. \`Super Repository\`. Either "Ultimate" or “My” must exist, “Repository” must always exist|

## Phrases

A phrase is a group of terms in a certain order. If you want to search for certain phrases then you can use the \`"\` operator.

|Definition|Example|
|----------|-------|
|"" - terms inside the quotes are searched for as a phrase|"Ultimate Repository" – finds \`Ultimate Repository\` but not \`Repository Ultimate\`, \`Ultimate\` or \`Repository\`|

`;
