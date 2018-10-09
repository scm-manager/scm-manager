// @flow
import {
  FAILURE_SUFFIX,
  PENDING_SUFFIX,
  SUCCESS_SUFFIX
} from "../../modules/types";
import { apiClient } from "@scm-manager/ui-components";
import type { Repository, Action, Branch } from "@scm-manager/ui-types";
import { isPending } from "../../modules/pending";

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

export default function reducer(
  state: Object = {},
  action: Action = { type: "UNKNOWN" }
): Object {
  switch (action.type) {
    case FETCH_BRANCHES_SUCCESS:
      const key = createKey(action.payload.repository);
      let oldBranchesByNames = { [key]: {} };
      if (state[key] !== undefined) {
        oldBranchesByNames[key] = state[key];
      }
      return {
        [key]: {
          byNames: extractBranchesByNames(
            action.payload.data,
            oldBranchesByNames[key].byNames
          )
        }
      };
    default:
      return state;
  }
}

function extractBranchesByNames(data: any, oldBranchesByNames: any): ?Object {
  if (!data._embedded || !data._embedded.branches) {
    return {};
  }
  const branches = data._embedded.branches;
  const branchesByNames = {};

  for (let branch of branches) {
    branchesByNames[branch.name] = branch;
  }

  for (let name in oldBranchesByNames) {
    branchesByNames[name] = oldBranchesByNames[name];
  }
  return branchesByNames;
}

// Selectors

export function getBranchNames(
  state: Object,
  repository: Repository
): ?Array<Branch> {
  const key = createKey(repository);
  if (!state.branches[key] || !state.branches[key].byNames) {
    return [];
  }
  return Object.keys(state.branches[key].byNames);
}

export function getBranches(state: Object, repository: Repository) {
  const key = createKey(repository);
  if (state.branches[key]) {
    if (state.branches[key].byNames) {
      return Object.values(state.branches[key].byNames);
    }
  }
}

export function getBranch(
  state: Object,
  repository: Repository,
  name: string
): Branch {
  const key = createKey(repository);
  if (state.branches[key]) {
    if (state.branches[key].byNames[name]) {
      return state.branches[key].byNames[name];
    }
  }
  return null;
}

export function isFetchBranchesPending(
  state: Object,
  repository: Repository
): boolean {
  return isPending(state, FETCH_BRANCHES, createKey(repository));
}

function createKey(repository: Repository): string {
  const { namespace, name } = repository;
  return `${namespace}/${name}`;
}
