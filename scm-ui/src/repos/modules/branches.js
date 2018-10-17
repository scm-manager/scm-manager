// @flow
import {
  FAILURE_SUFFIX,
  PENDING_SUFFIX,
  SUCCESS_SUFFIX
} from "../../modules/types";
import { apiClient } from "@scm-manager/ui-components";
import type { Action, Branch, Repository } from "@scm-manager/ui-types";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";

export const FETCH_BRANCHES = "scm/repos/FETCH_BRANCHES";
export const FETCH_BRANCHES_PENDING = `${FETCH_BRANCHES}_${PENDING_SUFFIX}`;
export const FETCH_BRANCHES_SUCCESS = `${FETCH_BRANCHES}_${SUCCESS_SUFFIX}`;
export const FETCH_BRANCHES_FAILURE = `${FETCH_BRANCHES}_${FAILURE_SUFFIX}`;

// Fetching branches

export function fetchBranches(repository: Repository) {
  if (!repository._links.branches) {
    return {
      type: FETCH_BRANCHES_SUCCESS,
      payload: { repository, data: {} },
      itemId: createKey(repository)
    };
  }

  return function(dispatch: any) {
    dispatch(fetchBranchesPending(repository));
    return apiClient
      .get(repository._links.branches.href)
      .then(response => response.json())
      .then(data => {
        dispatch(fetchBranchesSuccess(data, repository));
      })
      .catch(error => {
        dispatch(fetchBranchesFailure(repository, error));
      });
  };
}

// Action creators
export function fetchBranchesPending(repository: Repository) {
  return {
    type: FETCH_BRANCHES_PENDING,
    payload: { repository },
    itemId: createKey(repository)
  };
}

export function fetchBranchesSuccess(data: string, repository: Repository) {
  return {
    type: FETCH_BRANCHES_SUCCESS,
    payload: { data, repository },
    itemId: createKey(repository)
  };
}

export function fetchBranchesFailure(repository: Repository, error: Error) {
  return {
    type: FETCH_BRANCHES_FAILURE,
    payload: { error, repository },
    itemId: createKey(repository)
  };
}

// Reducers

type State = { [string]: Branch[] };

export default function reducer(
  state: State = {},
  action: Action = { type: "UNKNOWN" }
): State {
  switch (action.type) {
    case FETCH_BRANCHES_SUCCESS:
      const key = createKey(action.payload.repository);
      return {
        ...state,
        [key]: extractBranchesFromPayload(action.payload.data)
      };
    default:
      return state;
  }
}

function extractBranchesFromPayload(payload: any) {
  if (payload._embedded && payload._embedded.branches) {
    return payload._embedded.branches;
  }
  return [];
}

// Selectors

const empty = [];

export function getBranches(state: Object, repository: Repository) {
  const key = createKey(repository);
  if (state.branches[key]) {
    return state.branches[key];
  }
  return empty;
}

export function getBranch(
  state: State,
  repository: Repository,
  name: string
): ?Branch {
  const key = createKey(repository);
  if (state.branches[key]) {
    return state.branches[key].find((b: Branch) => b.name === name);
  }
  return null;
}

export function isFetchBranchesPending(
  state: Object,
  repository: Repository
): boolean {
  return isPending(state, FETCH_BRANCHES, createKey(repository));
}

export function getFetchBranchesFailure(state: Object, repository: Repository) {
  return getFailure(state, FETCH_BRANCHES, createKey(repository));
}

function createKey(repository: Repository): string {
  const { namespace, name } = repository;
  return `${namespace}/${name}`;
}
