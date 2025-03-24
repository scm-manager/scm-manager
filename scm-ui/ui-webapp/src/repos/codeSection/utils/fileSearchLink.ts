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

import { File, Repository } from "@scm-manager/ui-types";
import { urls } from "@scm-manager/ui-api";

export function getFileSearchLink(
  repository: Repository,
  revision: string,
  baseUrl: string,
  currentSource: File
): string {
  let currentSourcePath = null;

  if (repository.type === "svn" && currentSource.path) {
    currentSourcePath = urls.createPrevSourcePathQuery(`${revision}/${currentSource.path}`);
  } else if (repository.type === "svn") {
    currentSourcePath = urls.createPrevSourcePathQuery(`${revision}`);
  } else {
    currentSourcePath = urls.createPrevSourcePathQuery(currentSource.path);
  }

  if (currentSourcePath) {
    return `${baseUrl}/search/${encodeURIComponent(revision)}?${currentSourcePath}`;
  } else {
    return `${baseUrl}/search/${encodeURIComponent(revision)}`;
  }
}
