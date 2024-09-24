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

import { Branch, NamespaceAndName } from "@scm-manager/ui-types";

export const repoQueryKey = (repository: NamespaceAndName, ...values: unknown[]) => {
  return ["repository", repository.namespace, repository.name, ...values];
};

const isBranch = (branch: string | Branch): branch is Branch => {
  return (branch as Branch).name !== undefined;
};

export const branchQueryKey = (
  repository: NamespaceAndName,
  branch: string | Branch | undefined,
  ...values: unknown[]
) => {
  let branchName;
  if (!branch) {
    branchName = "_";
  } else if (isBranch(branch)) {
    branchName = branch.name;
  } else {
    branchName = branch;
  }
  return [...repoQueryKey(repository), "branch", branchName, ...values];
};
