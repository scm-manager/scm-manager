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

import { Person } from ".";
import { Embedded, HalRepresentation, HalRepresentationWithEmbedded, Links } from "./hal";

type EmbeddedBranches = {
  branches: Branch[];
} & Embedded;

export type BranchCollection = HalRepresentationWithEmbedded<EmbeddedBranches>;

export type Branch = {
  name: string;
  revision: string;
  defaultBranch?: boolean;
  lastCommitDate?: string;
  lastCommitter?: Person;
  stale?: boolean;
  _links: Links;
};

export type BranchDetails = HalRepresentation & {
  branchName: string;
  changesetsAhead?: number;
  changesetsBehind?: number;
};

type EmbeddedBranchDetails = {
  branchDetails: BranchDetails[];
} & Embedded;

export type BranchDetailsCollection = HalRepresentationWithEmbedded<EmbeddedBranchDetails>;

export type BranchCreation = {
  name: string;
  parent: string;
};

// @deprecated use BranchCreation instead
export type BranchRequest = BranchCreation;
