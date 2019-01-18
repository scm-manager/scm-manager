//@flow
import { apiClient } from "@scm-manager/ui-components";

export const CONTENT_TYPE_PERMISSIONS =
  "application/vnd.scmm-permissionCollection+json;v=2";

export function setPermissions(url: string, permissions: string[]) {
  return apiClient
    .put(url, { permissions: permissions }, CONTENT_TYPE_PERMISSIONS)
    .then(response => {
      return response;
    });
}
