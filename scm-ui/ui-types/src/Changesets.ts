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

import { Embedded, HalRepresentationWithEmbedded, Links, PagedCollection } from "./hal";
import { Tag } from "./Tags";
import { Branch } from "./Branches";
import { Person } from "./Person";
import { Signature } from "./Signature";

type ChangesetEmbedded = {
  tags?: Tag[];
  branches?: Branch[];
  parents?: ParentChangeset[];
} & Embedded;

export type Changeset = HalRepresentationWithEmbedded<ChangesetEmbedded> & {
  id: string;
  date: Date;
  author: Person;
  description: string;
  contributors?: Contributor[];
  signatures?: Signature[];
};

export type Contributor = {
  person: Person;
  type: string;
};

export type ParentChangeset = {
  id: string;
  _links: Links;
};

type EmbeddedChangesets = {
  changesets: Changeset[];
} & Embedded;

export type ChangesetCollection = PagedCollection<EmbeddedChangesets>;
