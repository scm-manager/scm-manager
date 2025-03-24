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

import { Branch, Changeset, File, Repository } from "@scm-manager/ui-types";

export type Description = {
  title: string;
  message: string;
};

export function createChangesetLink(repository: Repository, changeset: Changeset) {
  return `/repo/${repository.namespace}/${repository.name}/code/changeset/${changeset.id}`;
}

export function createChangesetLinkByBranch(repository: Repository, changeset: Changeset, branch: Branch) {
  if (!branch.name) {
    return `/repo/${repository.namespace}/${repository.name}/code/changeset/${changeset.id}`;
  } else {
    return `/repo/${repository.namespace}/${repository.name}/code/changeset/${changeset.id}?branch=${encodeURIComponent(
      branch.name
    )}`;
  }
}

export function createSourcesLink(repository: Repository, changeset: Changeset, file?: File) {
  let url = `/repo/${repository.namespace}/${repository.name}/code/sources/${changeset.id}`;

  if (file) {
    url += `/${encodeURIComponent(file.path)}/`;
  }
  return url;
}

export function parseDescription(description?: string): Description {
  const desc = description ? description : "";
  const lineBreak = desc.indexOf("\n");

  let title;
  let message = "";

  if (lineBreak > 0) {
    title = desc.substring(0, lineBreak);
    message = desc.substring(lineBreak + 1);
  } else {
    title = desc;
  }

  return {
    title,
    message,
  };
}
