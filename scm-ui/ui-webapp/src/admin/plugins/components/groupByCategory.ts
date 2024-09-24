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
