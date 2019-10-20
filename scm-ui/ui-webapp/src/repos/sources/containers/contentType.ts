import { apiClient } from "@scm-manager/ui-components";

export function getContentType(url: string) {
  return apiClient
    .head(url)
    .then(response => {
      return {
        type: response.headers.get("Content-Type"),
        language: response.headers.get("X-Programming-Language")
      };
    })
    .catch(err => {
      return {
        error: err
      };
    });
}
