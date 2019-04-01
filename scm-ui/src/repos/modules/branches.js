// @flow
import {
  FAILURE_SUFFIX,
  PENDING_SUFFIX,
  SUCCESS_SUFFIX
} from "../../modules/types";
import { apiClient } from "@scm-manager/ui-components";
import type {
  Action,
  Branch,
  Changeset,
  Repository
} from "@scm-manager/ui-types";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";

export const FETCH_BRANCHES = "scm/repos/FETCH_BRANCHES";
export const FETCH_BRANCHES_PENDING = `${FETCH_BRANCHES}_${PENDING_SUFFIX}`;
export const FETCH_BRANCHES_SUCCESS = `${FETCH_BRANCHES}_${SUCCESS_SUFFIX}`;
export const FETCH_BRANCHES_FAILURE = `${FETCH_BRANCHES}_${FAILURE_SUFFIX}`;

export const FETCH_BRANCH = "scm/repos/FETCH_BRANCH";
export const FETCH_BRANCH_PENDING = `${FETCH_BRANCH}_${PENDING_SUFFIX}`;
export const FETCH_BRANCH_SUCCESS = `${FETCH_BRANCH}_${SUCCESS_SUFFIX}`;
export const FETCH_BRANCH_FAILURE = `${FETCH_BRANCH}_${FAILURE_SUFFIX}`;

// Fetching branches

export function fetchBranchByName(link: string, name: string) {
  const branchUrl = link.endsWith("/")
    ? link + encodeURIComponent(name)
    : link + "/" + encodeURIComponent(name);
  return fetchBranch(branchUrl, name);
}

export function fetchBranchPending(name: string): Action {
  return {
    type: FETCH_BRANCH_PENDING,
    payload: name,
    itemId: name
  };
}

export function fetchBranchSuccess(branch: Branch): Action {
  return {
    type: FETCH_BRANCH_SUCCESS,
    payload: branch,
    itemId: branch.name
  };
}

export function fetchBranchFailure(name: string, error: Error): Action {
  return {
    type: FETCH_BRANCH_FAILURE,
    payload: name,
    itemId: name
  };
}

export function fetchBranch(link: string, name: string) {
  return function(dispatch: any) {
    dispatch(fetchBranchPending(name));
    return apiClient
      .get(link)
      .then(response => {
        return response.json();
      })
      .then(data => {
        dispatch(fetchBranchSuccess(data));
      })
      .catch(error => {
        dispatch(fetchBranchFailure(name, error));
      });
  };
}

export function getBranchByName(state: Object, name: string) {
  if (state.branches) {
    return state.branches[name];
  }
}

export function isFetchBranchPending(state: Object, name: string) {
  return isPending(state, FETCH_BRANCH, name);
}

export function getFetchBranchFailure(state: Object, name: string) {
  return getFailure(state, FETCH_BRANCH, name);
}

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
      return {
        ...state,
        [action.payload.name]: action.payload
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

export function createChangesetLink(repository: Repository, branch: Branch) {
  return `/repo/${repository.namespace}/${
    repository.name
  }/branch/${encodeURIComponent(branch.name)}/changesets/`;
}

export function createSourcesLink(repository: Repository, branch: Branch) {
  return `/repo/${repository.namespace}/${
    repository.name
  }/sources/${encodeURIComponent(branch.name)}/`;
}
