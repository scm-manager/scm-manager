// @flow
import {
  FAILURE_SUFFIX,
  PENDING_SUFFIX,
  SUCCESS_SUFFIX
} from "../../../modules/types";
import { apiClient } from "@scm-manager/ui-components";
import type { Action, Branch, Repository } from "@scm-manager/ui-types";
import { isPending } from "../../../modules/pending";
import { getFailure } from "../../../modules/failure";

export const FETCH_BRANCHES = "scm/repos/FETCH_BRANCHES";
export const FETCH_BRANCHES_PENDING = `${FETCH_BRANCHES}_${PENDING_SUFFIX}`;
export const FETCH_BRANCHES_SUCCESS = `${FETCH_BRANCHES}_${SUCCESS_SUFFIX}`;
export const FETCH_BRANCHES_FAILURE = `${FETCH_BRANCHES}_${FAILURE_SUFFIX}`;

export const FETCH_BRANCH = "scm/repos/FETCH_BRANCH";
export const FETCH_BRANCH_PENDING = `${FETCH_BRANCH}_${PENDING_SUFFIX}`;
export const FETCH_BRANCH_SUCCESS = `${FETCH_BRANCH}_${SUCCESS_SUFFIX}`;
export const FETCH_BRANCH_FAILURE = `${FETCH_BRANCH}_${FAILURE_SUFFIX}`;

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

export function fetchBranch(
  repository: Repository,
  name: string
) {
  let link = repository._links.branches.href;
  if (!link.endsWith("/")) {
    link += "/";
  }
  link += encodeURIComponent(name);
  return function(dispatch: any) {
    dispatch(fetchBranchPending(repository, name));
    return apiClient
      .get(link)
      .then(response => {
        return response.json();
      })
      .then(data => {
        dispatch(fetchBranchSuccess(repository, data));
      })
      .catch(error => {
        dispatch(fetchBranchFailure(repository, name, error));
      });
  };
}

// Selectors

export function getBranches(state: Object, repository: Repository) {
  const key = createKey(repository);
  if (state.branches[key]) {
    return state.branches[key];
  }
  return null;
}

export function getBranch(
  state: Object,
  repository: Repository,
  name: string
): ?Branch {
  const key = createKey(repository);
  if (state.branches[key]) {
    return state.branches[key].find((b: Branch) => b.name === name);
  }
  return null;
}

// Action creators
export function isFetchBranchesPending(
  state: Object,
  repository: Repository
): boolean {
  return isPending(state, FETCH_BRANCHES, createKey(repository));
}

export function getFetchBranchesFailure(state: Object, repository: Repository) {
  return getFailure(state, FETCH_BRANCHES, createKey(repository));
}

export function isFetchBranchPending(state: Object, repository: Repository, name: string) {
  return isPending(state, FETCH_BRANCH, createKey(repository) + "/" + name);
}

export function getFetchBranchFailure(state: Object, repository: Repository, name: string) {
  return getFailure(state, FETCH_BRANCH, createKey(repository) + "/" + name);
}

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

export function fetchBranchPending(
  repository: Repository,
  name: string
): Action {
  return {
    type: FETCH_BRANCH_PENDING,
    payload: { repository, name },
    itemId: createKey(repository) + "/" + name
  };
}

export function fetchBranchSuccess(
  repository: Repository,
  branch: Branch
): Action {
  return {
    type: FETCH_BRANCH_SUCCESS,
    payload: { repository, branch },
    itemId: createKey(repository) + "/" + branch.name
  };
}

export function fetchBranchFailure(
  repository: Repository,
  name: string,
  error: Error
): Action {
  return {
    type: FETCH_BRANCH_FAILURE,
    payload: { error, repository, name },
    itemId: createKey(repository) + "/" + name
  };
}

// Reducers

function extractBranchesFromPayload(payload: any) {
  if (payload._embedded && payload._embedded.branches) {
    return payload._embedded.branches;
  }
  return [];
}

function reduceBranchSuccess(state, repositoryName, newBranch) {
  const newBranches = [];
  // we do not use filter, because we try to keep the current order
  let found = false;
  for (const branch of state[repositoryName] || []) {
    if (branch.name === newBranch.name) {
      newBranches.push(newBranch);
      found = true;
    } else {
      newBranches.push(branch);
    }
  }
  if (!found) {
    newBranches.push(newBranch);
  }
  return newBranches;
}

type State = { [string]: Branch[] };

export default function reducer(
  state: State = {},
  action: Action = { type: "UNKNOWN" }
): State {
  if (!action.payload) {
    return state;
  }
  const payload = action.payload;
  switch (action.type) {
    case FETCH_BRANCHES_SUCCESS:
      const key = createKey(payload.repository);
      return {
        ...state,
        [key]: extractBranchesFromPayload(payload.data)
      };
    case FETCH_BRANCH_SUCCESS:
      if (!action.payload.repository || !action.payload.branch) {
        return state;
      }
      const newBranch = action.payload.branch;
      const repositoryName = createKey(action.payload.repository);
      return {
        ...state,
        [repositoryName]: reduceBranchSuccess(state, repositoryName, newBranch)
      };
    default:
      return state;
  }
}

function createKey(repository: Repository): string {
  const { namespace, name } = repository;
  return `${namespace}/${name}`;
}
