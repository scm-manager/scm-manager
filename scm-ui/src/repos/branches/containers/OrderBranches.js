// @flow

// master, default should always be the first one,
// followed by develop the rest should be ordered by its name
import type {Branch} from "@scm-manager/ui-types";

export function orderBranches(branches: Branch[]) {
  branches.sort((a, b) => {
    if (a.defaultBranch && !b.defaultBranch) {
      return -20;
    } else if (!a.defaultBranch && b.defaultBranch) {
      return 20;
    } else if (a.name === "master" && b.name !== "master") {
      return -10;
    } else if (a.name !== "master" && b.name === "master") {
      return 10;
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
  });
}
