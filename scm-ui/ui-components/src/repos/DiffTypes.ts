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
