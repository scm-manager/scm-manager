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

// master, default should always be the first one,
// followed by develop the rest should be ordered by its name
import { Branch } from "@scm-manager/ui-types";

export const SORT_OPTIONS = ["default", "name_asc", "name_desc"] as const;

export type SortOption = typeof SORT_OPTIONS[number];

export function orderBranches(branches: Branch[], sort?: SortOption) {
  branches.sort((a, b) => {
    switch (sort) {
      case "name_asc":
        return a.name > b.name ? 1 : -1;
      case "name_desc":
        return a.name > b.name ? -1 : 1;
      default:
        if (a.defaultBranch && !b.defaultBranch) {
          return -20;
        } else if (!a.defaultBranch && b.defaultBranch) {
          return 20;
        } else if (a.name === "main" && b.name !== "main") {
          return -10;
        } else if (a.name !== "main" && b.name === "main") {
          return 10;
        } else if (a.name === "master" && b.name !== "master") {
          return -9;
        } else if (a.name !== "master" && b.name === "master") {
          return 9;
        } else if (a.name === "default" && b.name !== "default") {
          return -10;
        } else if (a.name !== "default" && b.name === "default") {
          return 10;
        } else if (a.name === "develop" && b.name !== "develop") {
          return -5;
        } else if (a.name !== "develop" && b.name === "develop") {
          return 5;
        } else if (a.name < b.name) {
          return -1;
        } else if (a.name > b.name) {
          return 1;
        }
        return 0;
    }
  });
  return branches;
}
