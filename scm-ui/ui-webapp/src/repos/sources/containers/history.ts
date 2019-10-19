import { apiClient } from '@scm-manager/ui-components';

export function getHistory(url: string) {
  return apiClient
    .get(url)
    .then(response => response.json())
    .then(result => {
      return {
        changesets: result._embedded.changesets,
        pageCollection: {
          _embedded: result._embedded,
          _links: result._links,
          page: result.page,
          pageTotal: result.pageTotal,
        },
      };
    })
    .catch(err => {
      return {
        error: err,
      };
    });
}
