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

import * as diffs from "./diffs";

import {
  BaseContext,
  AnnotationFactory,
  AnnotationFactoryContext,
  DiffEventHandler,
  DiffEventContext,
  DiffObjectProps
} from "./DiffTypes";

import { FileDiff as File, FileChangeType, Hunk, Change, ChangeType } from "@scm-manager/ui-types";

export { diffs };

export * from "./annotate";
export * from "./changesets";

export { default as Diff } from "./Diff";
export { default as DiffFile } from "./DiffFile";
export { default as DiffButton } from "./DiffButton";
export { FileControlFactory } from "./DiffTypes";
export { default as LoadingDiff } from "./LoadingDiff";
export { DefaultCollapsed, DefaultCollapsedFunction } from "./defaultCollapsed";
export { default as RepositoryAvatar } from "./RepositoryAvatar";
export { default as RepositoryEntry } from "./RepositoryEntry";
export { default as RepositoryFlag } from "./RepositoryFlag";
export { default as JumpToFileButton } from "./JumpToFileButton";
export { default as CommitAuthor } from "./CommitAuthor";
export { default as HealthCheckFailureDetail } from "./HealthCheckFailureDetail";
export { default as RepositoryFlags } from "./RepositoryFlags";
export { default as DiffDropDown } from "./DiffDropDown";
export { default as DiffStatistics } from "./DiffStatistics"

export {
  File,
  FileChangeType,
  Hunk,
  Change,
  ChangeType,
  BaseContext,
  AnnotationFactory,
  AnnotationFactoryContext,
  DiffEventHandler,
  DiffEventContext,
  DiffObjectProps
};
