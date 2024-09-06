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

import { HalRepresentation, HalRepresentationWithEmbedded, PagedCollection } from "./hal";
import { Repository } from "./Repositories";

export type ValueHitField = {
  highlighted: false;
  value: unknown;
};

export type HighlightedHitField = {
  highlighted: true;
  fragments: string[];
  matchesContentStart: boolean;
  matchesContentEnd: boolean;
};

export type HitField = ValueHitField | HighlightedHitField;

export type EmbeddedRepository = {
  repository?: Repository;
};

export type Hit = HalRepresentationWithEmbedded<EmbeddedRepository> & {
  score: number;
  fields: { [name: string]: HitField };
};

export type HitEmbedded = {
  hits: Hit[];
};

export type QueryResult = PagedCollection<HitEmbedded> & {
  type: string;
  totalHits: number;
  queryType: "SIMPLE_WITH_ADDED_WILDCARDS" | "PARSED_EXPERT_QUERY";
};

export type SearchableField = {
  name: string;
  type: string;
};

export type SearchableType = HalRepresentation & {
  name: string;
  fields: SearchableField[];
};
