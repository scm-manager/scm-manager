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

import * as diffs from "./diffs";

import {
  BaseContext,
  AnnotationFactory,
  AnnotationFactoryContext,
  DiffEventHandler,
  DiffEventContext,
  DiffObjectProps,
} from "./DiffTypes";

import { FileDiff as File, FileChangeType, Hunk, Change, ChangeType } from "@scm-manager/ui-types";

export { diffs };

export * from "./annotate";
export * from "./changesets";

export { default as Diff } from "./Diff";
export { default as DiffFile } from "./DiffFile";
export { default as DiffButton } from "./DiffButton";
export { type FileControlFactory } from "./DiffTypes";
export { default as LoadingDiff, type WhitespaceMode } from "./LoadingDiff";
export { type DefaultCollapsed, type DefaultCollapsedFunction } from "./defaultCollapsed";
export { default as RepositoryAvatar } from "./RepositoryAvatar";
export { default as RepositoryEntry } from "./RepositoryEntry";
export { default as RepositoryFlag } from "./RepositoryFlag";
export { default as JumpToFileButton } from "./JumpToFileButton";
export { default as CommitAuthor } from "./CommitAuthor";
export { default as HealthCheckFailureDetail } from "./HealthCheckFailureDetail";
export { default as RepositoryFlags } from "./RepositoryFlags";
export { default as DiffDropDown } from "./DiffDropDown";
export { default as DiffStatistics } from "./DiffStatistics";
export { default as LayoutRadioButtons } from "./LayoutRadioButtons";
export { type LayoutMode, useLayoutState } from "./diffLayout";

export {
  type File,
  type FileChangeType,
  type Hunk,
  type Change,
  type ChangeType,
  type BaseContext,
  type AnnotationFactory,
  type AnnotationFactoryContext,
  type DiffEventHandler,
  type DiffEventContext,
  type DiffObjectProps,
};
