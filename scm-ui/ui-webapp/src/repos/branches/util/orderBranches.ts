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
