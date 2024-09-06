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

import { HalRepresentation, HalRepresentationWithEmbedded } from "./hal";

export type SubRepository = {
  repositoryUrl: string;
  browserUrl: string;
  revision: string;
};

export type File = HalRepresentationWithEmbedded<{
  children?: File[];
}> & {
  name: string;
  path: string;
  directory: boolean;
  description?: string;
  revision: string;
  length?: number;
  commitDate?: string;
  subRepository?: SubRepository;
  partialResult?: boolean;
  computationAborted?: boolean;
  truncated?: boolean;
};

export type Paths = HalRepresentation & {
  revision: string;
  paths: string[];
};
