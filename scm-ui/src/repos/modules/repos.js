// @flow
import { apiClient } from "../../apiclient";
import * as types from "../../modules/types";
import type { Action } from "../../types/Action";
import type { RepositoryCollection } from "../types/Repositories";
import {isPending} from "../../modules/pending";
import {getFailure} from "../../modules/failure";

export const FETCH_REPOS = "scm/repos/FETCH_REPOS";
export const FETCH_REPOS_PENDING = `${FETCH_REPOS}_${types.PENDING_SUFFIX}`;
export const FETCH_REPOS_SUCCESS = `${FETCH_REPOS}_${types.SUCCESS_SUFFIX}`;
export const FETCH_REPOS_FAILURE = `${FETCH_REPOS}_${types.FAILURE_SUFFIX}`;

const REPOS_URL = "repositories";
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

// reducer

function normalizeByNamespaceAndName(
  repositoryCollection: RepositoryCollection
) {
  const names = [];
  const byNames = {};
  for (const repository of repositoryCollection._embedded.repositories) {
    const identifier = repository.namespace + "/" + repository.name;
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

export default function reducer(
  state: Object = {},
  action: Action = { type: "UNKNOWN" }
): Object {
  if (action.type === FETCH_REPOS_SUCCESS) {
    if (action.payload) {
      return normalizeByNamespaceAndName(action.payload);
    } else {
      // TODO ???
      return state;
    }
  } else {
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
