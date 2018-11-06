//@flow
import { apiClient } from "@scm-manager/ui-components";
const CONTENT_TYPE_PASSWORD_OVERWRITE =
  "application/vnd.scmm-passwordOverwrite+json;v=2";
const CONTENT_TYPE_PASSWORD_CHANGE =
  "application/vnd.scmm-passwordChange+json;v=2";

export function setPassword(url: string, password: string) {
  return apiClient
    .put(url, { newPassword: password }, CONTENT_TYPE_PASSWORD_OVERWRITE)
    .then(response => {
      return response;
    })
    .catch(err => {
      return { error: err };
    });
}

export function updatePassword(
  url: string,
  oldPassword: string,
  newPassword: string
) {
  return apiClient
    .put(url, { oldPassword, newPassword }, CONTENT_TYPE_PASSWORD_CHANGE)
    .then(response => {
      return response;
    })
    .catch(err => {
      return { error: err };
    });
}
