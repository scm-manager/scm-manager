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

import { Repository } from "@scm-manager/ui-types";
import { binder, extensionPoints } from "@scm-manager/ui-extensions";
import { gitPredicate } from "./index";
import RepositoryConfig from "./RepositoryConfig";

const repository: Repository = { _links: {}, namespace: "hitchhiker", name: "HeartOfGold", type: "git" };

describe("test git predicate", () => {
  it("should return false", () => {
    expect(
      gitPredicate({
        repository: { ...repository, type: "hg" },
      })
    ).toBe(false);
  });

  it("should return true", () => {
    expect(
      gitPredicate({
        repository,
      })
    ).toBe(true);
  });
});

describe("test git repository settings", () => {
  const settingsFor = (type: string) =>
    binder.getExtensions<extensionPoints.RepoConfigDetails>("repo-config.details", {
      repository: { ...repository, type },
    });

  it("should be shown on the settings page of git repositories", () => {
    expect(settingsFor("git")).toContain(RepositoryConfig);
  });

  it("should not be shown for other repository types", () => {
    expect(settingsFor("hg")).not.toContain(RepositoryConfig);
  });
});
