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

import { File } from "@scm-manager/ui-types";
import { ContentType } from "@scm-manager/ui-api";

export const isRootPath = (path: string) => {
  return path === "" || path === "/";
};

export const isRootFile = (file: File) => {
  if (!file?.directory) {
    return false;
  }
  return isRootPath(file.path);
};

export const isEmptyDirectory = (file: File) => {
  if (!file?.directory) {
    return false;
  }
  return (file._embedded?.children?.length || 0) === 0;
};

export const determineSyntaxHighlightingLanguage = (data: ContentType) => {
  return data.prismMode || data.codemirrorMode || data.aceMode || data.language;
};
