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
import { MemoryRouter } from "react-router-dom";
import { render, screen } from "@testing-library/react";
import EditRepoNavLink from "./EditRepoNavLink";

describe("GeneralNavLink", () => {
  it("should render nothing, if the modify link is missing", () => {
    const repository = {
      namespace: "space",
      name: "name",
      type: "git",
      _links: {},
    };

    render(
      <MemoryRouter>
        <EditRepoNavLink repository={repository} editUrl="" />
      </MemoryRouter>,
    );
    assert.notExists(screen.queryByText("repositoryRoot.menu.generalNavLink"));
  });

  it("should render the navLink", () => {
    const repository = {
      namespace: "space",
      name: "name",
      type: "git",
      _links: {
        update: {
          href: "/repositories",
        },
      },
    };

    render(
      <MemoryRouter>
        <EditRepoNavLink repository={repository} editUrl="" />
      </MemoryRouter>,
    );

    assert.exists(screen.queryByText("repositoryRoot.menu.generalNavLink"));
  });
});
