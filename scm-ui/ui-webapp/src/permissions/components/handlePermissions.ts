import { apiClient } from "@scm-manager/ui-components";

export const CONTENT_TYPE_PERMISSIONS = "application/vnd.scmm-permissionCollection+json;v=2";

export function setPermissions(url: string, permissions: string[]) {
  return apiClient
    .put(
      url,
      {
        permissions: permissions
      },
      CONTENT_TYPE_PERMISSIONS
    )
    .then(response => {
      return response;
    });
}

export function loadPermissionsForEntity(availableUrl: string, userUrl: string) {
  return Promise.all([
    apiClient.get(availableUrl).then(response => {
      return response.json();
    }),
    apiClient.get(userUrl).then(response => {
      return response.json();
    })
  ]).then(values => {
    const [availablePermissions, checkedPermissions] = values;
    const permissions = {};
    availablePermissions.permissions.forEach(p => (permissions[p] = false));
    checkedPermissions.permissions.forEach(p => (permissions[p] = true));
    return {
      permissions,
      overwriteLink: checkedPermissions._links.overwrite
    };
  });
}
