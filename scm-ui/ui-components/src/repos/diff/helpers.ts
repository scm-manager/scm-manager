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

import { Change, FileDiff, Hunk as HunkType } from "@scm-manager/ui-types";
import { escapeWhitespace } from "../diffs";
import { AnnotationFactory } from "../DiffTypes";

const EMPTY_ANNOTATION_FACTORY = {};

export const collectHunkAnnotations = (hunk: HunkType, file: FileDiff, annotationFactory?: AnnotationFactory) => {
  if (annotationFactory) {
    return annotationFactory({
      hunk,
      file,
    });
  } else {
    return EMPTY_ANNOTATION_FACTORY;
  }
};

export const getAnchorId = (file: FileDiff) => {
  let path: string;
  if (file.type === "delete") {
    path = file.oldPath;
  } else {
    path = file.newPath;
  }
  return escapeWhitespace(path);
};

/**
 * Creates a key that is unique for each hunk of a file. The content of a hunk cannot be used for this, because it is
 * empty for every hunk that was added by expanding the diff.
 */
export const getHunkKey = (hunk: HunkType) => `-${hunk.oldStart},${hunk.oldLines} +${hunk.newStart},${hunk.newLines}`;

export const hasContent = (file: FileDiff) => file && !file.isBinary && file.hunks && file.hunks.length > 0;

export const hoverFileTitle = (file: FileDiff): string => {
  if (file.oldPath !== file.newPath && (file.type === "copy" || file.type === "rename")) {
    return `${file.oldPath} > ${file.newPath}`;
  } else if (file.type === "delete") {
    return file.oldPath;
  }
  return file.newPath;
};

export const findConflicts = (hunk: HunkType) => {
  const conflicts: Change[] = [];
  let inConflict = false;
  for (let i = 0; i < hunk.changes.length; ++i) {
    if (hunk.changes[i].content === "<<<<<<< HEAD") {
      inConflict = true;
    }
    if (inConflict) {
      conflicts.push(hunk.changes[i]);
    }
    if (hunk.changes[i].content.startsWith(">>>>>>>")) {
      inConflict = false;
    }
  }
  return conflicts;
};
