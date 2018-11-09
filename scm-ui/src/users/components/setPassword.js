//@flow
import { apiClient } from "@scm-manager/ui-components";

export const CONTENT_TYPE_PASSWORD_OVERWRITE =
  "application/vnd.scmm-passwordOverwrite+json;v=2";

export function setPassword(url: string, password: string) {
  return apiClient
    .put(url, { newPassword: password }, CONTENT_TYPE_PASSWORD_OVERWRITE)
    .then(response => {
      return response;
    });
}
