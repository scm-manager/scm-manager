//@flow
import * as validator from "./permissionValidation";

describe("permission validation", () => {
  it("should return true if permission is valid and does not exist", () => {
    const permissions = [];
    const name = "PermissionName";
    const groupPermission = false;

    expect(
      validator.isPermissionValid(name, groupPermission, permissions)
    ).toBe(true);
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

    expect(
      validator.isPermissionValid(name, groupPermission, permissions)
    ).toBe(true);
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

    expect(
      validator.isPermissionValid(name, groupPermission, permissions)
    ).toBe(false);
  });

  it("should return false if permission does not exist but is invalid", () => {
    const permissions = [];
    const name = "@PermissionName";
    const groupPermission = false;

    expect(
      validator.isPermissionValid(name, groupPermission, permissions)
    ).toBe(false);
  });

  it("should return false if permission is not valid and does not exist", () => {
    const permissions = [];
    const name = "@PermissionName";
    const groupPermission = false;

    expect(
      validator.isPermissionValid(name, groupPermission, permissions)
    ).toBe(false);
  });
});
