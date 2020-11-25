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

import { apiClient } from "@scm-manager/ui-components";
import * as types from "../../modules/types";
import {
  Action,
  Namespace,
  NamespaceCollection,
  Repository,
  RepositoryCollection,
  RepositoryCreation,
  RepositoryImport,
  Link
} from "@scm-manager/ui-types";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";

export const FETCH_REPOS = "scm/repos/FETCH_REPOS";
export const FETCH_REPOS_PENDING = `${FETCH_REPOS}_${types.PENDING_SUFFIX}`;
export const FETCH_REPOS_SUCCESS = `${FETCH_REPOS}_${types.SUCCESS_SUFFIX}`;
export const FETCH_REPOS_FAILURE = `${FETCH_REPOS}_${types.FAILURE_SUFFIX}`;

export const FETCH_NAMESPACES = "scm/repos/FETCH_NAMESPACES";
export const FETCH_NAMESPACES_PENDING = `${FETCH_NAMESPACES}_${types.PENDING_SUFFIX}`;
export const FETCH_NAMESPACES_SUCCESS = `${FETCH_NAMESPACES}_${types.SUCCESS_SUFFIX}`;
export const FETCH_NAMESPACES_FAILURE = `${FETCH_NAMESPACES}_${types.FAILURE_SUFFIX}`;

export const FETCH_REPO = "scm/repos/FETCH_REPO";
export const FETCH_REPO_PENDING = `${FETCH_REPO}_${types.PENDING_SUFFIX}`;
export const FETCH_REPO_SUCCESS = `${FETCH_REPO}_${types.SUCCESS_SUFFIX}`;
export const FETCH_REPO_FAILURE = `${FETCH_REPO}_${types.FAILURE_SUFFIX}`;

export const CREATE_REPO = "scm/repos/CREATE_REPO";
export const CREATE_REPO_PENDING = `${CREATE_REPO}_${types.PENDING_SUFFIX}`;
export const CREATE_REPO_SUCCESS = `${CREATE_REPO}_${types.SUCCESS_SUFFIX}`;
export const CREATE_REPO_FAILURE = `${CREATE_REPO}_${types.FAILURE_SUFFIX}`;
export const CREATE_REPO_RESET = `${CREATE_REPO}_${types.RESET_SUFFIX}`;

export const IMPORT_REPO = "scm/repos/IMPORT_REPO";
export const IMPORT_REPO_PENDING = `${IMPORT_REPO}_${types.PENDING_SUFFIX}`;
export const IMPORT_REPO_SUCCESS = `${IMPORT_REPO}_${types.SUCCESS_SUFFIX}`;
export const IMPORT_REPO_FAILURE = `${IMPORT_REPO}_${types.FAILURE_SUFFIX}`;
export const IMPORT_REPO_RESET = `${IMPORT_REPO}_${types.RESET_SUFFIX}`;

export const MODIFY_REPO = "scm/repos/MODIFY_REPO";
export const MODIFY_REPO_PENDING = `${MODIFY_REPO}_${types.PENDING_SUFFIX}`;
export const MODIFY_REPO_SUCCESS = `${MODIFY_REPO}_${types.SUCCESS_SUFFIX}`;
export const MODIFY_REPO_FAILURE = `${MODIFY_REPO}_${types.FAILURE_SUFFIX}`;
export const MODIFY_REPO_RESET = `${MODIFY_REPO}_${types.RESET_SUFFIX}`;

export const DELETE_REPO = "scm/repos/DELETE_REPO";
export const DELETE_REPO_PENDING = `${DELETE_REPO}_${types.PENDING_SUFFIX}`;
export const DELETE_REPO_SUCCESS = `${DELETE_REPO}_${types.SUCCESS_SUFFIX}`;
export const DELETE_REPO_FAILURE = `${DELETE_REPO}_${types.FAILURE_SUFFIX}`;

export const FETCH_NAMESPACE = "scm/repos/FETCH_NAMESPACE";
export const FETCH_NAMESPACE_PENDING = `${FETCH_NAMESPACE}_${types.PENDING_SUFFIX}`;
export const FETCH_NAMESPACE_SUCCESS = `${FETCH_NAMESPACE}_${types.SUCCESS_SUFFIX}`;
export const FETCH_NAMESPACE_FAILURE = `${FETCH_NAMESPACE}_${types.FAILURE_SUFFIX}`;

export const CONTENT_TYPE = "application/vnd.scmm-repository+json;v=2";

export const CUSTOM_NAMESPACE_STRATEGY = "CustomNamespaceStrategy";

// fetch repos

const SORT_BY = "sortBy=namespaceAndName";

export function fetchRepos(link: string) {
  return fetchReposByLink(link);
}

export function fetchReposByPage(link: string, page: number, filter?: string) {
  const linkWithPage = `${link}?page=${page - 1}`;
  if (filter) {
    return fetchReposByLink(`${linkWithPage}&q=${decodeURIComponent(filter)}`);
  }
  return fetchReposByLink(linkWithPage);
}

function appendSortByLink(url: string) {
  if (url.includes(SORT_BY)) {
    return url;
  }
  let urlWithSortBy = url;
  if (url.includes("?")) {
    urlWithSortBy += "&";
  } else {
    urlWithSortBy += "?";
  }
  return urlWithSortBy + SORT_BY;
}

export function fetchReposByLink(link: string) {
  const url = appendSortByLink(link);
  return function(dispatch: any) {
    dispatch(fetchReposPending());
    return apiClient
      .get(url)
      .then(response => response.json())
      .then(repositories => {
        dispatch(fetchReposSuccess(repositories));
      })
      .catch(err => {
        dispatch(fetchReposFailure(err));
      });
  };
}

export function fetchReposPending(): Action {
  return {
    type: FETCH_REPOS_PENDING
  };
}

export function fetchReposSuccess(repositories: RepositoryCollection): Action {
  return {
    type: FETCH_REPOS_SUCCESS,
    payload: repositories
  };
}

export function fetchReposFailure(err: Error): Action {
  return {
    type: FETCH_REPOS_FAILURE,
    payload: err
  };
}

// fetch namespaces
export function fetchNamespaces(link: string) {
  return function(dispatch: any) {
    dispatch(fetchNamespacesPending());
    return apiClient
      .get(link)
      .then(response => response.json())
      .then(namespaces => {
        dispatch(fetchNamespacesSuccess(namespaces));
      })
      .catch(err => {
        dispatch(fetchNamespacesFailure(err));
      });
  };
}

export function fetchNamespacesPending(): Action {
  return {
    type: FETCH_NAMESPACES_PENDING
  };
}

export function fetchNamespacesSuccess(namespaces: NamespaceCollection): Action {
  return {
    type: FETCH_NAMESPACES_SUCCESS,
    payload: namespaces
  };
}

export function fetchNamespacesFailure(err: Error): Action {
  return {
    type: FETCH_NAMESPACES_FAILURE,
    payload: err
  };
}

// fetch repo
export function fetchRepoByLink(repo: Repository) {
  return fetchRepo((repo._links.self as Link).href, repo.namespace, repo.name);
}

export function fetchRepoByName(link: string, namespace: string, name: string) {
  const repoUrl = link.endsWith("/") ? link : link + "/";
  return fetchRepo(`${repoUrl}${namespace}/${name}`, namespace, name);
}

function fetchRepo(link: string, namespace: string, name: string) {
  return function(dispatch: any) {
    dispatch(fetchRepoPending(namespace, name));
    return apiClient
      .get(link)
      .then(response => response.json())
      .then(repository => {
        dispatch(fetchRepoSuccess(repository));
      })
      .catch(err => {
        dispatch(fetchRepoFailure(namespace, name, err));
      });
  };
}

export function fetchRepoPending(namespace: string, name: string): Action {
  return {
    type: FETCH_REPO_PENDING,
    payload: {
      namespace,
      name
    },
    itemId: namespace + "/" + name
  };
}

export function fetchRepoSuccess(repository: Repository): Action {
  return {
    type: FETCH_REPO_SUCCESS,
    payload: repository,
    itemId: createIdentifier(repository)
  };
}

export function fetchRepoFailure(namespace: string, name: string, error: Error): Action {
  return {
    type: FETCH_REPO_FAILURE,
    payload: {
      namespace,
      name,
      error
    },
    itemId: namespace + "/" + name
  };
}

// import repo

export function importRepoFromUrl(link: string, repository: RepositoryImport, callback?: (repo: Repository) => void) {
  const importLink = link + `import/${repository.type}/url`;
  return function(dispatch: any) {
    dispatch(importRepoPending());
    return apiClient
      .post(importLink, repository, CONTENT_TYPE)
      .then(response => {
        const location = response.headers.get("Location");
        dispatch(importRepoSuccess());
        return apiClient.get(location);
      })
      .then(response => response.json())
      .then(response => {
        if (callback) {
          callback(response);
        }
      })
      .catch(err => {
        dispatch(importRepoFailure(err));
      });
  };
}

export function importRepoPending(): Action {
  return {
    type: IMPORT_REPO_PENDING
  };
}

export function importRepoSuccess(): Action {
  return {
    type: IMPORT_REPO_SUCCESS
  };
}

export function importRepoFailure(err: Error): Action {
  return {
    type: IMPORT_REPO_FAILURE,
    payload: err
  };
}

// create repo

export function createRepo(
  link: string,
  repository: RepositoryCreation,
  initRepository: boolean,
  callback?: (repo: Repository) => void
) {
  return function(dispatch: any) {
    dispatch(createRepoPending());
    const repoLink = initRepository ? link + "?initialize=true" : link;
    return apiClient
      .post(repoLink, repository, CONTENT_TYPE)
      .then(response => {
        const location = response.headers.get("Location");
        dispatch(createRepoSuccess());
        return apiClient.get(location);
      })
      .then(response => response.json())
      .then(response => {
        if (callback) {
          callback(response);
        }
      })
      .catch(err => {
        dispatch(createRepoFailure(err));
      });
  };
}

export function createRepoPending(): Action {
  return {
    type: CREATE_REPO_PENDING
  };
}

export function createRepoSuccess(): Action {
  return {
    type: CREATE_REPO_SUCCESS
  };
}

export function createRepoFailure(err: Error): Action {
  return {
    type: CREATE_REPO_FAILURE,
    payload: err
  };
}

export function createRepoReset(): Action {
  return {
    type: CREATE_REPO_RESET
  };
}

// modify

export function modifyRepo(repository: Repository, callback?: () => void) {
  return function(dispatch: any) {
    dispatch(modifyRepoPending(repository));

    return apiClient
      .put((repository._links.update as Link).href, repository, CONTENT_TYPE)
      .then(() => {
        dispatch(modifyRepoSuccess(repository));
        if (callback) {
          callback();
        }
      })
      .then(() => {
        dispatch(fetchRepoByLink(repository));
      })
      .catch(error => {
        dispatch(modifyRepoFailure(repository, error));
      });
  };
}

export function modifyRepoPending(repository: Repository): Action {
  return {
    type: MODIFY_REPO_PENDING,
    payload: repository,
    itemId: createIdentifier(repository)
  };
}

export function modifyRepoSuccess(repository: Repository): Action {
  return {
    type: MODIFY_REPO_SUCCESS,
    payload: repository,
    itemId: createIdentifier(repository)
  };
}

export function modifyRepoFailure(repository: Repository, error: Error): Action {
  return {
    type: MODIFY_REPO_FAILURE,
    payload: {
      error,
      repository
    },
    itemId: createIdentifier(repository)
  };
}

export function modifyRepoReset(repository: Repository): Action {
  return {
    type: MODIFY_REPO_RESET,
    payload: {
      repository
    },
    itemId: createIdentifier(repository)
  };
}

// delete

export function deleteRepo(repository: Repository, callback?: () => void) {
  return function(dispatch: any) {
    dispatch(deleteRepoPending(repository));
    return apiClient
      .delete((repository._links.delete as Link).href)
      .then(() => {
        dispatch(deleteRepoSuccess(repository));
        if (callback) {
          callback();
        }
      })
      .catch(err => {
        dispatch(deleteRepoFailure(repository, err));
      });
  };
}

export function deleteRepoPending(repository: Repository): Action {
  return {
    type: DELETE_REPO_PENDING,
    payload: repository,
    itemId: createIdentifier(repository)
  };
}

export function deleteRepoSuccess(repository: Repository): Action {
  return {
    type: DELETE_REPO_SUCCESS,
    payload: repository,
    itemId: createIdentifier(repository)
  };
}

export function deleteRepoFailure(repository: Repository, error: Error): Action {
  return {
    type: DELETE_REPO_FAILURE,
    payload: {
      error,
      repository
    },
    itemId: createIdentifier(repository)
  };
}

export function fetchNamespace(link: string, namespaceName: string) {
  return function(dispatch: any) {
    dispatch(fetchNamespacePending(namespaceName));
    return apiClient
      .get(link)
      .then(response => response.json())
      .then(namespace => {
        dispatch(fetchNamespaceSuccess(namespace));
      })
      .catch(err => {
        dispatch(fetchNamespaceFailure(namespaceName, err));
      });
  };
}

export function fetchNamespacePending(namespaceName: string): Action {
  return {
    type: FETCH_NAMESPACE_PENDING,
    payload: {
      namespaceName
    },
    itemId: namespaceName
  };
}

export function fetchNamespaceSuccess(namespace: Namespace): Action {
  return {
    type: FETCH_NAMESPACE_SUCCESS,
    payload: namespace,
    itemId: namespace.namespace
  };
}

export function fetchNamespaceFailure(namespaceName: string, error: Error): Action {
  return {
    type: FETCH_NAMESPACE_FAILURE,
    payload: {
      namespaceName,
      error
    },
    itemId: namespaceName
  };
}

// reducer

function createIdentifier(repository: Repository) {
  return repository.namespace + "/" + repository.name;
}

function normalizeByNamespaceAndName(state: object, repositoryCollection: RepositoryCollection) {
  const names = [];
  const byNames = {};
  for (const repository of repositoryCollection._embedded.repositories) {
    const identifier = createIdentifier(repository);
    names.push(identifier);
    byNames[identifier] = repository;
  }
  return {
    ...state,
    list: {
      ...repositoryCollection,
      _embedded: {
        repositories: names
      }
    },
    byNames: byNames
  };
}

const reducerByNames = (state: object, repository: Repository) => {
  const identifier = createIdentifier(repository);
  return {
    ...state,
    byNames: {
      ...state.byNames,
      [identifier]: repository
    }
  };
};

const reducerForNamespace = (state: object, namespace: Namespace) => {
  const identifier = namespace.namespace;
  return {
    ...state,
    namespacesByNames: {
      ...state.namespacesByNames,
      [identifier]: namespace
    }
  };
};

const reducerForNamespaces = (state: object, namespaces: NamespaceCollection) => {
  return {
    ...state,
    namespaces: namespaces
  };
};

export default function reducer(
  state: object = {},
  action: Action = {
    type: "UNKNOWN"
  }
): object {
  if (!action.payload) {
    return state;
  }

  switch (action.type) {
    case FETCH_REPOS_SUCCESS:
      return normalizeByNamespaceAndName(state, action.payload);
    case FETCH_NAMESPACES_SUCCESS:
      return reducerForNamespaces(state, action.payload);
    case FETCH_REPO_SUCCESS:
      return reducerByNames(state, action.payload);
    case FETCH_NAMESPACE_SUCCESS:
      return reducerForNamespace(state, action.payload);
    default:
      return state;
  }
}

// selectors

export function getRepositoryCollection(state: object) {
  if (state.repos && state.repos.list && state.repos.byNames) {
    const repositories = [];
    for (const repositoryName of state.repos.list._embedded.repositories) {
      repositories.push(state.repos.byNames[repositoryName]);
    }
    return {
      ...state.repos.list,
      _embedded: {
        repositories
      }
    };
  }
}

export function getNamespaceCollection(state: object) {
  return state.repos.namespaces;
}

export function isFetchReposPending(state: object) {
  return isPending(state, FETCH_REPOS);
}

export function getFetchReposFailure(state: object) {
  return getFailure(state, FETCH_REPOS);
}

export function getRepository(state: object, namespace: string, name: string) {
  if (state.repos && state.repos.byNames) {
    return state.repos.byNames[namespace + "/" + name];
  }
}

export function isFetchNamespacesPending(state: object) {
  return isPending(state, FETCH_NAMESPACES);
}

export function getFetchNamespacesFailure(state: object) {
  return getFailure(state, FETCH_NAMESPACES);
}

export function isFetchNamespacePending(state: object) {
  return isPending(state, FETCH_NAMESPACE);
}

export function getFetchNamespaceFailure(state: object) {
  return getFailure(state, FETCH_NAMESPACE);
}

export function fetchNamespaceByName(link: string, namespaceName: string) {
  const namespaceUrl = link.endsWith("/") ? link : link + "/";
  return fetchNamespace(`${namespaceUrl}${namespaceName}`, namespaceName);
}

export function isFetchRepoPending(state: object, namespace: string, name: string) {
  return isPending(state, FETCH_REPO, namespace + "/" + name);
}

export function getFetchRepoFailure(state: object, namespace: string, name: string) {
  return getFailure(state, FETCH_REPO, namespace + "/" + name);
}

export function getNamespace(state: object, namespaceName: string) {
  if (state.repos && state.repos.namespacesByNames) {
    return state.repos.namespacesByNames[namespaceName];
  }
}

export function isAbleToCreateRepos(state: object) {
  return !!(state.repos && state.repos.list && state.repos.list._links && state.repos.list._links.create);
}

export function isImportRepoPending(state: object) {
  return isPending(state, IMPORT_REPO);
}

export function getImportRepoFailure(state: object) {
  return getFailure(state, IMPORT_REPO);
}

export function isCreateRepoPending(state: object) {
  return isPending(state, CREATE_REPO);
}

export function getCreateRepoFailure(state: object) {
  return getFailure(state, CREATE_REPO);
}

export function isModifyRepoPending(state: object, namespace: string, name: string) {
  return isPending(state, MODIFY_REPO, namespace + "/" + name);
}

export function getModifyRepoFailure(state: object, namespace: string, name: string) {
  return getFailure(state, MODIFY_REPO, namespace + "/" + name);
}

export function isDeleteRepoPending(state: object, namespace: string, name: string) {
  return isPending(state, DELETE_REPO, namespace + "/" + name);
}

export function getDeleteRepoFailure(state: object, namespace: string, name: string) {
  return getFailure(state, DELETE_REPO, namespace + "/" + name);
}

export function getPermissionsLink(state: object, namespaceName: string, repoName?: string) {
  if (repoName) {
    const repo = getRepository(state, namespaceName, repoName);
    return repo?._links ? repo._links.permissions.href : undefined;
  } else {
    const namespace = getNamespace(state, namespaceName);
    return namespace?._links ? namespace?._links?.permissions?.href : undefined;
  }
}
