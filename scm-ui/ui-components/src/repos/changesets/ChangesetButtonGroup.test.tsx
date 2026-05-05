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

import React from "react";
import { BrowserRouter } from "react-router-dom";
import { render } from "@testing-library/react";
import { Changeset, Repository } from "@scm-manager/ui-types";
import * as changesets from "./changesets";
import ChangesetButtonGroup from "./ChangesetButtonGroup";

vi.mock("react-router", async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...(actual as Record<string, unknown>),
    // Mock the 'Link' component to render an <a> tag directly, bypassing the broken router context (Fixes basename error)
    Link: vi.fn(({ to, children, ...props }) => (
      <a href={to} {...props}>
        {children}
      </a>
    )),
  };
});

const createChangesetLink = vi.spyOn(changesets, "createChangesetLink").mockReturnValue("/changeset");
const createChangesetLinkByBranch = vi.spyOn(changesets, "createChangesetLinkByBranch").mockReturnValue("/branch");

afterEach(() => {
  vi.resetAllMocks();
});

describe("ChangesetButtonGroup", () => {
  it("should call createChangesetLink without branch", () => {
    const { repository, changeset } = createTestData();
    render(
      <BrowserRouter>
        <ChangesetButtonGroup repository={repository} changeset={changeset}></ChangesetButtonGroup>
      </BrowserRouter>,
    );
    expect(createChangesetLink).toHaveBeenCalled();
    expect(createChangesetLinkByBranch).toHaveBeenCalledTimes(0);
  });

  it("should call createChangesetLink by branch with branch", () => {
    const { repository, changeset, sourceName } = createTestData();
    render(
      <BrowserRouter>
        <ChangesetButtonGroup
          repository={repository}
          changeset={changeset}
          sourceName={sourceName}
        ></ChangesetButtonGroup>
      </BrowserRouter>,
    );
    expect(createChangesetLinkByBranch).toHaveBeenCalled();
    expect(createChangesetLink).toHaveBeenCalledTimes(0);
  });
});

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
  return { repository, changeset, sourceName: "develop" };
}
