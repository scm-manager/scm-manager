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

import { HalRepresentation, Links } from "./hal";

export type FileChangeType = "add" | "modify" | "delete" | "copy" | "rename";

export type Diff = HalRepresentation & {
  files: FileDiff[];
  partial: boolean;
  statistics?: Statistics;
};

export type FileDiff = {
  hunks?: Hunk[];
  newEndingNewLine: boolean;
  newMode?: string;
  newPath: string;
  newRevision?: string;
  oldEndingNewLine: boolean;
  oldMode?: string;
  oldPath: string;
  oldRevision?: string;
  type: FileChangeType;
  language?: string;
  syntaxModes?: { [mode: string]: string };
  // TODO does this property exists?
  isBinary?: boolean;
  _links?: Links;
};

export type Statistics = {
  added: number;
  deleted: number;
  modified: number;
}

export type Hunk = {
  changes: Change[];
  content: string;
  oldStart?: number;
  newStart?: number;
  oldLines?: number;
  newLines?: number;
  fullyExpanded?: boolean;
  expansion?: boolean;
};

export type ChangeType = "insert" | "delete" | "normal" | "conflict";

export type Change = {
  content: string;
  isNormal?: boolean;
  isInsert?: boolean;
  isDelete?: boolean;
  lineNumber?: number;
  newLineNumber?: number;
  oldLineNumber?: number;
  type: ChangeType;
};
