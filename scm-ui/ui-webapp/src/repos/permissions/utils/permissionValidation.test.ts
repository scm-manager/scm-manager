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

import * as validator from "./permissionValidation";

describe("permission validation", () => {
  it("should return true if permission is valid and does not exist", () => {
    const permissions = [];
    const name = "PermissionName";
    const groupPermission = false;

    expect(validator.isPermissionValid(name, groupPermission, permissions)).toBe(true);
  });

  it("should return true if permission is valid and does not exists with same group permission", () => {
    const permissions = [
      {
        name: "PermissionName",
        groupPermission: true,
        type: "READ",
        _links: {},
        verbs: []
      }
    ];
    const name = "PermissionName";
    const groupPermission = false;

    expect(validator.isPermissionValid(name, groupPermission, permissions)).toBe(true);
  });

  it("should return false if permission is valid but exists", () => {
    const permissions = [
      {
        name: "PermissionName",
        groupPermission: false,
        type: "READ",
        _links: {},
        verbs: []
      }
    ];
    const name = "PermissionName";
    const groupPermission = false;

    expect(validator.isPermissionValid(name, groupPermission, permissions)).toBe(false);
  });

  it("should return false if permission does not exist but is invalid", () => {
    const permissions = [];
    const name = "@PermissionName";
    const groupPermission = false;

    expect(validator.isPermissionValid(name, groupPermission, permissions)).toBe(false);
  });

  it("should return false if permission is not valid and does not exist", () => {
    const permissions = [];
    const name = "@PermissionName";
    const groupPermission = false;

    expect(validator.isPermissionValid(name, groupPermission, permissions)).toBe(false);
  });
});
