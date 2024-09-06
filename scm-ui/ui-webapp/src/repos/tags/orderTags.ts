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

// sort tags by date beginning with latest first
import { Tag } from "@scm-manager/ui-types";

export const SORT_OPTIONS = ["default", "name_asc", "name_desc"] as const;

export type SortOption = typeof SORT_OPTIONS[number];

export default (tags: Tag[], sort?: SortOption) => {
  return tags.sort((a, b) => {
    switch (sort) {
      case "name_asc":
        return a.name > b.name ? 1 : -1;
      case "name_desc":
        return a.name > b.name ? -1 : 1;
      default:
        // @ts-ignore Comparing dates is a valid operation. It is unknown why typescript shows an error here.
        return new Date(b.date) - new Date(a.date);
    }
  });
};
