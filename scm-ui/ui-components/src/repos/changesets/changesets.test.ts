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

import { createChangesetLink, createChangesetLinkByBranch, parseDescription } from "./changesets";
import { Branch, Changeset, Repository } from "@scm-manager/ui-types";

describe("createChangesetLink", () => {
  it("should return a changeset link", () => {
    const { repository, changeset } = createTestData();
    const link = createChangesetLink(repository, changeset);
    expect(link).toBe("/repo/sandbox/anarchy/code/changeset/4f153aa670d4b27c");
  });
});

describe("createChangesetLinkByBranch", () => {
  it("should return a changeset link with a branch query with given branch", () => {
    const { repository, changeset, branch } = createTestData();
    const link = createChangesetLinkByBranch(repository, changeset, branch);
    expect(link).toBe("/repo/sandbox/anarchy/code/changeset/4f153aa670d4b27c?branch=resonanceCascade");
  });
  it("should return no branch query parameter with empty string", () => {
    const { repository, changeset, branch } = createTestData();
    branch.name = "";
    const link = createChangesetLinkByBranch(repository, changeset, branch);
    expect(link).toBe("/repo/sandbox/anarchy/code/changeset/4f153aa670d4b27c");
  });
  it("should escape a branch with a slash inside", () => {
    const { repository, changeset, branch } = createTestData();
    branch.name = "feature/rescueWorld";
    const link = createChangesetLinkByBranch(repository, changeset, branch);
    expect(link).toBe("/repo/sandbox/anarchy/code/changeset/4f153aa670d4b27c?branch=feature%2FrescueWorld");
  });
});

describe("parseDescription", () => {
  it("should return a description with title and message", () => {
    const desc = parseDescription("Hello\nTrillian");
    expect(desc.title).toBe("Hello");
    expect(desc.message).toBe("Trillian");
  });

  it("should return a description with title and without message", () => {
    const desc = parseDescription("Hello Trillian");
    expect(desc.title).toBe("Hello Trillian");
  });

  it("should return an empty description for undefined", () => {
    const desc = parseDescription();
    expect(desc.title).toBe("");
    expect(desc.message).toBe("");
  });
});

function createTestData() {
  const repository: Repository = {
    name: "anarchy",
    namespace: "sandbox",
    type: "git",
    _links: {},
  };

  const changeset: Changeset = {
    author: {
      name: "Gordon Freeman",
    },
    date: new Date(),
    description: "Some repository.",
    id: "4f153aa670d4b27c",
    _links: {},
  };

  const branch: Branch = {
    name: "resonanceCascade",
    revision: "4f153aa670d4b27c",
    _links: {},
  };

  return {
    repository,
    changeset,
    branch,
  };
}
