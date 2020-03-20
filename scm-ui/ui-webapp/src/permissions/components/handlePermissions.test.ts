/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
    fetchMock.getOnce("/api/v2" + AVAILABLE_PERMISSIONS_URL, availablePermissions);
    fetchMock.getOnce("/api/v2" + USER_PERMISSIONS_URL, userPermissions);
  });

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should return permissions array", done => {
    loadPermissionsForEntity(AVAILABLE_PERMISSIONS_URL, USER_PERMISSIONS_URL).then(result => {
      const { permissions } = result;
      expect(Object.entries(permissions).length).toBe(3);
      expect(permissions["repository:read,pull:*"]).toBe(true);
      expect(permissions["repository:read,pull,push:*"]).toBe(false);
      expect(permissions["repository:*:*"]).toBe(false);
      done();
    });
  });

  it("should return overwrite link", done => {
    loadPermissionsForEntity(AVAILABLE_PERMISSIONS_URL, USER_PERMISSIONS_URL).then(result => {
      const { overwriteLink } = result;
      expect(overwriteLink.href).toBe("/api/v2/users/rene/permissions");
      done();
    });
  });
});
