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

import * as changesets from "./changesets";
import { render } from "@testing-library/react";
import { Branch, Changeset, Repository } from "@scm-manager/ui-types";
import ChangesetButtonGroup from "./ChangesetButtonGroup";
import React from "react";
import { BrowserRouter } from "react-router-dom";
import { stubI18Next } from "@scm-manager/ui-tests";

const createChangesetLink = jest.spyOn(changesets, "createChangesetLink");
const createChangesetLinkByBranch = jest.spyOn(changesets, "createChangesetLinkByBranch");

afterEach(() => {
  jest.resetAllMocks();
});

describe("ChangesetButtonGroup", () => {
  test("shouldCallCreateChangesetLinkWithoutBranch", async () => {
    stubI18Next();
    const { repository, changeset } = createTestData();
    render(
      <BrowserRouter>
        <ChangesetButtonGroup repository={repository} changeset={changeset}></ChangesetButtonGroup>
      </BrowserRouter>
    );
    expect(createChangesetLink).toHaveBeenCalled();
    expect(createChangesetLinkByBranch).toHaveBeenCalledTimes(0);
  });

  test("shouldCallCreateChangesetLinkByBranchWithBranch", async () => {
    stubI18Next();
    const { repository, changeset, branch } = createTestData();
    render(
      <BrowserRouter>
        <ChangesetButtonGroup repository={repository} changeset={changeset} branch={branch}></ChangesetButtonGroup>
      </BrowserRouter>
    );
    expect(createChangesetLinkByBranch).toHaveBeenCalled();
    expect(createChangesetLink).toHaveBeenCalledTimes(0);
  });
});

// TODO centralized test data
function createTestData() {
  const repository: Repository = { _links: {}, name: "", namespace: "", type: "" };
  const changeset: Changeset = {
    _links: {},
    author: {
      name: "",
    },
    date: new Date(),
    description: "",
    id: "",
  };
  const branch: Branch = { _links: {}, name: "", revision: "" };
  return { repository, changeset, branch };
}
