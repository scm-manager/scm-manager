import fetchMock from "fetch-mock";
import { loadPermissionsForEntity } from "./handlePermissions";

describe("load permissions for entity", () => {
  const AVAILABLE_PERMISSIONS_URL = "/permissions";
  const USER_PERMISSIONS_URL = "/user/scmadmin/permissions";

  const availablePermissions = `{
      "permissions": [
        "repository:read,pull:*",
        "repository:read,pull,push:*",
        "repository:*:*"
      ]
    }`;
  const userPermissions = `{
      "permissions": [
        "repository:read,pull:*"
      ],
      "_links": {
        "self": {
          "href": "/api/v2/users/rene/permissions"
        },
        "overwrite": {
          "href": "/api/v2/users/rene/permissions"
        }
      }
    }`;

  beforeEach(() => {
    fetchMock.getOnce(
      "/api/v2" + AVAILABLE_PERMISSIONS_URL,
      availablePermissions
    );
    fetchMock.getOnce("/api/v2" + USER_PERMISSIONS_URL, userPermissions);
  });

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should return permissions array", done => {
    loadPermissionsForEntity(
      AVAILABLE_PERMISSIONS_URL,
      USER_PERMISSIONS_URL
    ).then(result => {
      const { permissions } = result;
      expect(Object.entries(permissions).length).toBe(3);
      expect(permissions["repository:read,pull:*"]).toBe(true);
      expect(permissions["repository:read,pull,push:*"]).toBe(false);
      expect(permissions["repository:*:*"]).toBe(false);
      done();
    });
  });

  it("should return overwrite link", done => {
    loadPermissionsForEntity(
      AVAILABLE_PERMISSIONS_URL,
      USER_PERMISSIONS_URL
    ).then(result => {
      const { overwriteLink } = result;
      expect(overwriteLink.href).toBe("/api/v2/users/rene/permissions");
      done();
    });
  });
});
