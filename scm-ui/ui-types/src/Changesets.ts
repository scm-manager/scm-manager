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
  time?: Date;
};

export type ParentChangeset = {
  id: string;
  _links: Links;
};

type EmbeddedChangesets = {
  changesets: Changeset[];
} & Embedded;

export type ChangesetCollection = PagedCollection<EmbeddedChangesets>;
