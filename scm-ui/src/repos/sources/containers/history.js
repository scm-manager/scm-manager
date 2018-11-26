//@flow
import { apiClient } from "@scm-manager/ui-components";

export function getHistory(url: string) {
  return apiClient
    .get(url)
    .then(response => response.json())
    .then(result => {
      return {
        changesets: result._embedded.changesets
      };
    })
    .catch(err => {
      return { error: err };
    });
}
