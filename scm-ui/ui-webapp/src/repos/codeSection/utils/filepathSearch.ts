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

import "string_score";

// typing for string_score
declare global {
  interface String {
    score(query: string): number;
  }
}

export const filepathSearch = (paths: string[], query: string): string[] => {
  return paths
    .map(createMatcher(query))
    .filter(m => m.matches)
    .sort((a, b) => b.score - a.score)
    .slice(0, 50)
    .map(m => m.path);
};

const includes = (value: string, query: string) => {
  return value.toLocaleLowerCase("en").includes(query.toLocaleLowerCase("en"));
};

export const createMatcher = (query: string) => {
  return (path: string) => {
    const parts = path.split("/");
    const filename = parts[parts.length - 1];

    let score = filename.score(query);
    if (score > 0 && includes(filename, query)) {
      score += 0.5;
    } else if (score <= 0) {
      score = path.score(query) * 0.25;
    }

    return {
      matches: score > 0,
      score,
      path
    };
  };
};
