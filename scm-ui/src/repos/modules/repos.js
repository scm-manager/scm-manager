// @flow
import { apiClient } from "../../apiclient";
import * as types from "../../modules/types";
import type { Action } from "../../types/Action";
import type { RepositoryCollection } from "../types/Repositories";

export const FETCH_REPOS = "scm/repos/FETCH_REPOS";
export const FETCH_REPOS_PENDING = `${FETCH_REPOS}_${types.PENDING_SUFFIX}`;
export const FETCH_REPOS_SUCCESS = `${FETCH_REPOS}_${types.SUCCESS_SUFFIX}`;
export const FETCH_REPOS_FAILURE = `${FETCH_REPOS}_${types.FAILURE_SUFFIX}`;

const REPOS_URL = "repositories";

export function fetchRepos() {
  return function(dispatch: any) {
    dispatch(fetchReposPending());
    return apiClient
      .get(REPOS_URL)
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
  switch (action.type) {
    case FETCH_REPOS_SUCCESS:
      if (action.payload) {
        return normalizeByNamespaceAndName(action.payload);
      } else {
        // TODO ???
        return state;
      }
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
