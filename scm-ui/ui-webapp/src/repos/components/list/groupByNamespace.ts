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

import { NamespaceCollection, Repository, RepositoryGroup } from "@scm-manager/ui-types";

export default function groupByNamespace(
  repositories: Repository[],
  namespaces: NamespaceCollection
): RepositoryGroup[] {
  const groups: Record<string, RepositoryGroup> = {};
  for (const repository of repositories) {
    const groupName = repository.namespace;

    let group = groups[groupName];
    if (!group) {
      const namespace = findNamespace(namespaces, groupName);
      group = {
        name: groupName,
        namespace: namespace,
        repositories: [],
      };
      groups[groupName] = group;
    }
    group.repositories.push(repository);
  }

  const groupArray: RepositoryGroup[] = [];
  for (const groupName in groups) {
    groupArray.push(groups[groupName]);
  }
  groupArray.sort(sortByName);
  applyOffsets(groupArray);
  return groupArray;
}

function sortByName(a: RepositoryGroup, b: RepositoryGroup) {
  if (a.name < b.name) {
    return -1;
  } else if (a.name > b.name) {
    return 1;
  }
  return 0;
}

function applyOffsets(groups: RepositoryGroup[]) {
  let offset = 0;
  for (const group of groups) {
    group.currentPageOffset = offset;
    offset += group.repositories.length;
  }
}

function findNamespace(namespaces: NamespaceCollection, namespaceToFind: string) {
  return namespaces._embedded.namespaces.find((namespace) => namespace.namespace === namespaceToFind);
}
