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

import { ReactNode } from "react";
import { DefaultCollapsed } from "./defaultCollapsed";
import { Change, Hunk, FileDiff as File } from "@scm-manager/ui-types";

export type ChangeEvent = {
  change: Change;
};

export type BaseContext = {
  hunk: Hunk;
  file: File;
};

export type AnnotationFactoryContext = BaseContext;

export type FileAnnotationFactory = (file: File) => ReactNode[];

// key = change id, value = react component
export type AnnotationFactory = (context: AnnotationFactoryContext) => {
  [key: string]: any;
};

export type DiffEventContext = BaseContext & {
  changeId: string;
  change: Change;
};

export type DiffEventHandler = (context: DiffEventContext) => void;

export type FileControlFactory = (file: File, setCollapseState: (p: boolean) => void) => ReactNode | null | undefined;

export type DiffObjectProps = {
  sideBySide?: boolean;
  whitespace?: boolean;
  onClick?: DiffEventHandler;
  fileControlFactory?: FileControlFactory;
  fileAnnotationFactory?: FileAnnotationFactory;
  annotationFactory?: AnnotationFactory;
  markConflicts?: boolean;
  defaultCollapse?: DefaultCollapsed;
  isCollapsed?: (file: File) => boolean;
  onCollapseStateChange?: (file: File, newState?: boolean) => void;
  hunkClass?: (hunk: Hunk) => string;
  /**
   * Toggle whether header of diff should be sticky
   * If truthy, numeric value adds distance to top position
   */
  stickyHeader?: boolean | number;
  /**
   * Fontawesome Icon Unicode
   *
   * @see https://fontawesome.com/icons
   * @example
   * "\f075"
   */
  hunkGutterHoverIcon?: string;
  highlightLineOnHover?: boolean;
};
