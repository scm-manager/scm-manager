import { apiClient } from '@scm-manager/ui-components';

export const CONTENT_TYPE_PASSWORD_CHANGE =
  'application/vnd.scmm-passwordChange+json;v=2';
export function changePassword(
  url: string,
  oldPassword: string,
  newPassword: string,
) {
  return apiClient
    .put(
      url,
      {
        oldPassword,
        newPassword,
      },
      CONTENT_TYPE_PASSWORD_CHANGE,
    )
    .then(response => {
      return response;
    });
}
