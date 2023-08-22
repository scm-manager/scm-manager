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

import { NamespaceCollection, Repository, RepositoryGroup } from "@scm-manager/ui-types";

export default function groupByNamespace(
  repositories: Repository[],
  namespaces: NamespaceCollection
): RepositoryGroup[] {
  const groups = {};
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

  const groupArray = [];
  for (const groupName in groups) {
    groupArray.push(groups[groupName]);
  }
  groupArray.sort(sortByName);
  return groupArray;
}

function sortByName(a, b) {
  if (a.name < b.name) {
    return -1;
  } else if (a.name > b.name) {
    return 1;
  }
  return 0;
}

function findNamespace(namespaces: NamespaceCollection, namespaceToFind: string) {
  return namespaces._embedded.namespaces.find((namespace) => namespace.namespace === namespaceToFind);
}
