// @flow
import { apiClient } from "../../apiclient";
import * as types from "../../modules/types";
import type { Action } from "../../types/Action";
import type { Repository, RepositoryCollection } from "../types/Repositories";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";

export const FETCH_REPOS = "scm/repos/FETCH_REPOS";
export const FETCH_REPOS_PENDING = `${FETCH_REPOS}_${types.PENDING_SUFFIX}`;
export const FETCH_REPOS_SUCCESS = `${FETCH_REPOS}_${types.SUCCESS_SUFFIX}`;
export const FETCH_REPOS_FAILURE = `${FETCH_REPOS}_${types.FAILURE_SUFFIX}`;

export const FETCH_REPO = "scm/repos/FETCH_REPO";
export const FETCH_REPO_PENDING = `${FETCH_REPO}_${types.PENDING_SUFFIX}`;
export const FETCH_REPO_SUCCESS = `${FETCH_REPO}_${types.SUCCESS_SUFFIX}`;
export const FETCH_REPO_FAILURE = `${FETCH_REPO}_${types.FAILURE_SUFFIX}`;

export const CREATE_REPO = "scm/repos/CREATE_REPO";
export const CREATE_REPO_PENDING = `${CREATE_REPO}_${types.PENDING_SUFFIX}`;
export const CREATE_REPO_SUCCESS = `${CREATE_REPO}_${types.SUCCESS_SUFFIX}`;
export const CREATE_REPO_FAILURE = `${CREATE_REPO}_${types.FAILURE_SUFFIX}`;
export const CREATE_REPO_RESET = `${CREATE_REPO}_${types.RESET_SUFFIX}`;

export const DELETE_REPO = "scm/repos/DELETE_REPO";
export const DELETE_REPO_PENDING = `${DELETE_REPO}_${types.PENDING_SUFFIX}`;
export const DELETE_REPO_SUCCESS = `${DELETE_REPO}_${types.SUCCESS_SUFFIX}`;
export const DELETE_REPO_FAILURE = `${DELETE_REPO}_${types.FAILURE_SUFFIX}`;

const REPOS_URL = "repositories";

const CONTENT_TYPE = "application/vnd.scmm-repository+json;v=2";

// fetch repos

const SORT_BY = "sortBy=namespaceAndName";

export function fetchRepos() {
  return fetchReposByLink(REPOS_URL);
}

export function fetchReposByPage(page: number) {
  return fetchReposByLink(`${REPOS_URL}?page=${page - 1}`);
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

// fetch repo

export function fetchRepo(namespace: string, name: string) {
  return function(dispatch: any) {
    dispatch(fetchRepoPending(namespace, name));
    return apiClient
      .get(`${REPOS_URL}/${namespace}/${name}`)
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

export function fetchRepoFailure(
  namespace: string,
  name: string,
  error: Error
): Action {
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

// create repo

export function createRepo(repository: Repository, callback?: () => void) {
  return function(dispatch: any) {
    dispatch(createRepoPending());
    return apiClient
      .post(REPOS_URL, repository, CONTENT_TYPE)
      .then(() => {
        dispatch(createRepoSuccess());
        if (callback) {
          callback();
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

// delete

export function deleteRepo(repository: Repository, callback?: () => void) {
  return function(dispatch: any) {
    dispatch(deleteRepoPending(repository));
    return apiClient
      .delete(repository._links.delete.href)
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

export function deleteRepoFailure(
  repository: Repository,
  error: Error
): Action {
  return {
    type: DELETE_REPO_FAILURE,
    payload: {
      error,
      repository
    },
    itemId: createIdentifier(repository)
  };
}

// reducer

function createIdentifier(repository: Repository) {
  return repository.namespace + "/" + repository.name;
}

function normalizeByNamespaceAndName(
  repositoryCollection: RepositoryCollection
) {
  const names = [];
  const byNames = {};
  for (const repository of repositoryCollection._embedded.repositories) {
    const identifier = createIdentifier(repository);
    names.push(identifier);
    byNames[identifier] = repository;
  }
  return {
    list: {
      ...repositoryCollection,
      _embedded: {
        repositories: names
      }
    },
    byNames: byNames
  };
}

const reducerByNames = (state: Object, repository: Repository) => {
  const identifier = createIdentifier(repository);
  const newState = {
    ...state,
    byNames: {
      ...state.byNames,
      [identifier]: repository
    }
  };

  return newState;
};

export default function reducer(
  state: Object = {},
  action: Action = { type: "UNKNOWN" }
): Object {
  if (!action.payload) {
    return state;
  }

  switch (action.type) {
    case FETCH_REPOS_SUCCESS:
      return normalizeByNamespaceAndName(action.payload);
    case FETCH_REPO_SUCCESS:
      return reducerByNames(state, action.payload);
    default:
      return state;
  }
}

// selectors

export function getRepositoryCollection(state: Object) {
  if (state.repos && state.repos.list && state.repos.byNames) {
    const repositories = [];
    for (let repositoryName of state.repos.list._embedded.repositories) {
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

export function isFetchReposPending(state: Object) {
  return isPending(state, FETCH_REPOS);
}

export function getFetchReposFailure(state: Object) {
  return getFailure(state, FETCH_REPOS);
}

export function getRepository(state: Object, namespace: string, name: string) {
  if (state.repos && state.repos.byNames) {
    return state.repos.byNames[namespace + "/" + name];
  }
}

export function isFetchRepoPending(
  state: Object,
  namespace: string,
  name: string
) {
  return isPending(state, FETCH_REPO, namespace + "/" + name);
}

export function getFetchRepoFailure(
  state: Object,
  namespace: string,
  name: string
) {
  return getFailure(state, FETCH_REPO, namespace + "/" + name);
}

export function isAbleToCreateRepos(state: Object) {
  return !!(
    state.repos &&
    state.repos.list &&
    state.repos.list._links &&
    state.repos.list._links.create
  );
}

export function isCreateRepoPending(state: Object) {
  return isPending(state, CREATE_REPO);
}

export function getCreateRepoFailure(state: Object) {
  return getFailure(state, CREATE_REPO);
}

export function isDeleteRepoPending(
  state: Object,
  namespace: string,
  name: string
) {
  return isPending(state, DELETE_REPO, namespace + "/" + name);
}

export function getDeleteRepoFailure(
  state: Object,
  namespace: string,
  name: string
) {
  return getFailure(state, DELETE_REPO, namespace + "/" + name);
}
