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
import "@scm-manager/ui-tests";
import SetPermissionsNavLink from "./SetPermissionsNavLink";
import { shallow } from "enzyme";

it("should render nothing, if the permissions link is missing", () => {
  const user = {
    displayName: "User",
    name: "User",
    password: "hitchhiker",
    external: true,
    active: true,
    _links: {}
  };

  const navLink = shallow(<SetPermissionsNavLink user={user} permissionsUrl="/user/permissions" />);
  expect(navLink.text()).toBe("");
});

it("should render the navLink", () => {
  const user = {
    displayName: "User",
    name: "User",
    password: "hitchhiker",
    external: true,
    active: true,
    _links: {
      permissions: {
        href: "/permissions"
      }
    }
  };

  const navLink = shallow(<SetPermissionsNavLink user={user} permissionsUrl="/user/permissions" />);
  expect(navLink.text()).not.toBe("");
});
