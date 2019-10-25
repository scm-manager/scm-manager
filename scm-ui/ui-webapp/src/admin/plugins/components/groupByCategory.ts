import { Plugin, PluginGroup } from "@scm-manager/ui-types";

export default function groupByCategory(plugins: Plugin[]): PluginGroup[] {
  const groups = {};
  for (const plugin of plugins) {
    const groupName = plugin.category;

    let group = groups[groupName];
    if (!group) {
      group = {
        name: groupName,
        plugins: []
      };
      groups[groupName] = group;
    }
    group.plugins.push(plugin);
  }

  const groupArray = [];
  for (const groupName in groups) {
    const group = groups[groupName];
    group.plugins.sort(sortByName);
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
