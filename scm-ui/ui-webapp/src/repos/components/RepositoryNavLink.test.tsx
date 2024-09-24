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
import { mount, shallow } from "@scm-manager/ui-tests";
import "@scm-manager/ui-tests";
import { LocalStorageProvider } from "@scm-manager/ui-api";
import RepositoryNavLink from "./RepositoryNavLink";

describe("RepositoryNavLink", () => {
  it("should render nothing, if the sources link is missing", () => {
    const repository = {
      namespace: "Namespace",
      name: "Repo",
      type: "GIT",
      _links: {},
    };

    const navLink = shallow(
      <RepositoryNavLink
        repository={repository}
        linkName="sources"
        to="/sources"
        label="Sources"
        activeOnlyWhenExact={true}
      />
    );
    expect(navLink.text()).toBe("");
  });

  it("should render the navLink", () => {
    const repository = {
      namespace: "Namespace",
      name: "Repo",
      type: "GIT",
      _links: {
        sources: {
          href: "/sources",
        },
      },
    };

    const navLink = mount(
      <LocalStorageProvider>
        <RepositoryNavLink
          repository={repository}
          linkName="sources"
          to="/sources"
          label="Sources"
          activeOnlyWhenExact={true}
        />
      </LocalStorageProvider>
    );
    expect(navLink.text()).toBe("Sources");
  });
});
