// @flow
import type { Repository, RepositoryGroup } from "../../types/Repositories";

export default function groupByNamespace(
  repositories: Repository[]
): RepositoryGroup[] {
  let groups = {};
  for (let repository of repositories) {
    const groupName = repository.namespace;

    let group = groups[groupName];
    if (!group) {
      group = {
        name: groupName,
        repositories: []
      };
      groups[groupName] = group;
    }
    group.repositories.push(repository);
  }

  let groupArray = [];
  for (let groupName in groups) {
    const group = groups[groupName];
    group.repositories.sort(sortByName);
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
