// @flow

import * as types from "../../modules/types";
import type { Action } from "../../types/Action";
import type {
  RepositoryType,
  RepositoryTypeCollection
} from "../types/RepositoryTypes";
import { apiClient } from "../../apiclient";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";

export const FETCH_REPOSITORY_TYPES = "scm/repos/FETCH_REPOSITORY_TYPES";
export const FETCH_REPOSITORY_TYPES_PENDING = `${FETCH_REPOSITORY_TYPES}_${
  types.PENDING_SUFFIX
}`;
export const FETCH_REPOSITORY_TYPES_SUCCESS = `${FETCH_REPOSITORY_TYPES}_${
  types.SUCCESS_SUFFIX
}`;
export const FETCH_REPOSITORY_TYPES_FAILURE = `${FETCH_REPOSITORY_TYPES}_${
  types.FAILURE_SUFFIX
}`;

export function fetchRepositoryTypesIfNeeded() {
  return function(dispatch: any, getState: () => Object) {
    if (shouldFetchRepositoryTypes(getState())) {
      return fetchRepositoryTypes(dispatch);
    }
  };
}

function fetchRepositoryTypes(dispatch: any) {
  dispatch(fetchRepositoryTypesPending());
  return apiClient
    .get("repository-types")
    .then(response => response.json())
    .then(repositoryTypes => {
      dispatch(fetchRepositoryTypesSuccess(repositoryTypes));
    })
    .catch(err => {
      dispatch(fetchRepositoryTypesFailure(err));
    });
}

export function shouldFetchRepositoryTypes(state: Object) {
  if (
    isFetchRepositoryTypesPending(state) ||
    getFetchRepositoryTypesFailure(state)
  ) {
    return false;
  }
  if (state.repositoryTypes && state.repositoryTypes.length > 0) {
    return false;
  }
  return true;
}

export function fetchRepositoryTypesPending(): Action {
  return {
    type: FETCH_REPOSITORY_TYPES_PENDING
  };
}

export function fetchRepositoryTypesSuccess(
  repositoryTypes: RepositoryTypeCollection
): Action {
  return {
    type: FETCH_REPOSITORY_TYPES_SUCCESS,
    payload: repositoryTypes
  };
}

export function fetchRepositoryTypesFailure(error: Error): Action {
  return {
    type: FETCH_REPOSITORY_TYPES_FAILURE,
    payload: error
  };
}

// reducers

export default function reducer(
  state: RepositoryType[] = [],
  action: Action = { type: "UNKNOWN" }
): RepositoryType[] {
  if (action.type === FETCH_REPOSITORY_TYPES_SUCCESS && action.payload) {
    return action.payload._embedded["repository-types"];
  }
  return state;
}

// selectors

export function getRepositoryTypes(state: Object) {
  if (state.repositoryTypes) {
    return state.repositoryTypes;
  }
  return [];
}

export function isFetchRepositoryTypesPending(state: Object) {
  return isPending(state, FETCH_REPOSITORY_TYPES);
}

export function getFetchRepositoryTypesFailure(state: Object) {
  return getFailure(state, FETCH_REPOSITORY_TYPES);
}
