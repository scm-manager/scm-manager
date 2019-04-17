// @flow
import {
  FAILURE_SUFFIX,
  PENDING_SUFFIX,
  SUCCESS_SUFFIX,
  RESET_SUFFIX
} from "../../../modules/types";
import { apiClient } from "@scm-manager/ui-components";
import type {
  Action,
  Branch,
  BranchRequest,
  Repository
} from "@scm-manager/ui-types";
import { isPending } from "../../../modules/pending";
import { getFailure } from "../../../modules/failure";

import memoizeOne from 'memoize-one';

export const FETCH_BRANCHES = "scm/repos/FETCH_BRANCHES";
export const FETCH_BRANCHES_PENDING = `${FETCH_BRANCHES}_${PENDING_SUFFIX}`;
export const FETCH_BRANCHES_SUCCESS = `${FETCH_BRANCHES}_${SUCCESS_SUFFIX}`;
export const FETCH_BRANCHES_FAILURE = `${FETCH_BRANCHES}_${FAILURE_SUFFIX}`;

export const FETCH_BRANCH = "scm/repos/FETCH_BRANCH";
export const FETCH_BRANCH_PENDING = `${FETCH_BRANCH}_${PENDING_SUFFIX}`;
export const FETCH_BRANCH_SUCCESS = `${FETCH_BRANCH}_${SUCCESS_SUFFIX}`;
export const FETCH_BRANCH_FAILURE = `${FETCH_BRANCH}_${FAILURE_SUFFIX}`;

export const CREATE_BRANCH = "scm/repos/CREATE_BRANCH";
export const CREATE_BRANCH_PENDING = `${CREATE_BRANCH}_${PENDING_SUFFIX}`;
export const CREATE_BRANCH_SUCCESS = `${CREATE_BRANCH}_${SUCCESS_SUFFIX}`;
export const CREATE_BRANCH_FAILURE = `${CREATE_BRANCH}_${FAILURE_SUFFIX}`;
export const CREATE_BRANCH_RESET = `${CREATE_BRANCH}_${RESET_SUFFIX}`;

const CONTENT_TYPE_BRANCH_REQUEST =
  "application/vnd.scmm-branchRequest+json;v=2";

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

export function fetchBranch(repository: Repository, name: string) {
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

// create branch

export function createBranch(
  link: string,
  repository: Repository,
  branchRequest: BranchRequest,
  callback?: (branch: Branch) => void
) {
  return function(dispatch: any) {
    dispatch(createBranchPending(repository));
    return apiClient
      .post(link, branchRequest, CONTENT_TYPE_BRANCH_REQUEST)
      .then(response => response.headers.get("Location"))
      .then(location => apiClient.get(location))
      .then(response => response.json())
      .then(branch => {
        dispatch(createBranchSuccess(repository));
        if (callback) {
          callback(branch);
        }
      })
      .catch(error => dispatch(createBranchFailure(repository, error)));
  };
}

// Selectors

function collectBranches(repoState) {
  return repoState.list._embedded.branches.map(
    name => repoState.byName[name]
  );
}

const memoizedBranchCollector = memoizeOne(collectBranches);

export function getBranches(state: Object, repository: Repository) {
  const repoState = getRepoState(state, repository);
  if (repoState && repoState.list) {
    return memoizedBranchCollector(repoState);
  }
}

export function getBranchCreateLink(state: Object, repository: Repository) {
  const repoState = getRepoState(state, repository);
  if (repoState && repoState.list && repoState.list._links && repoState.list._links.create) {
    return repoState.list._links.create.href;
  }
}

function getRepoState(state: Object, repository: Repository) {
  const key = createKey(repository);
  const repoState = state.branches[key];
  if (repoState && repoState.byName) {
    return repoState;
  }
}

export const isPermittedToCreateBranches = (
  state: Object,
  repository: Repository
): boolean => {
  const repoState = getRepoState(state, repository);
  return !!(
    repoState &&
    repoState.list &&
    repoState.list._links &&
    repoState.list._links.create
  );
};

export function getBranch(
  state: Object,
  repository: Repository,
  name: string
): ?Branch {
  const repoState = getRepoState(state, repository);
  if (repoState) {
    return repoState.byName[name];
  }
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

export function isCreateBranchPending(state: Object, repository: Repository) {
  return isPending(state, CREATE_BRANCH, createKey(repository));
}

export function getCreateBranchFailure(state: Object) {
  return getFailure(state, CREATE_BRANCH);
}

export function createBranchPending(repository: Repository): Action {
  return {
    type: CREATE_BRANCH_PENDING,
    payload: { repository },
    itemId: createKey(repository)
  };
}

export function createBranchSuccess(repository: Repository): Action {
  return {
    type: CREATE_BRANCH_SUCCESS,
    payload: { repository },
    itemId: createKey(repository)
  };
}

export function createBranchFailure(
  repository: Repository,
  error: Error
): Action {
  return {
    type: CREATE_BRANCH_FAILURE,
    payload: { repository, error },
    itemId: createKey(repository)
  };
}

export function createBranchReset(repository: Repository): Action {
  return {
    type: CREATE_BRANCH_RESET,
    payload: { repository },
    itemId: createKey(repository)
  };
}

export function isFetchBranchPending(
  state: Object,
  repository: Repository,
  name: string
) {
  return isPending(state, FETCH_BRANCH, createKey(repository) + "/" + name);
}

export function getFetchBranchFailure(
  state: Object,
  repository: Repository,
  name: string
) {
  return getFailure(state, FETCH_BRANCH, createKey(repository) + "/" + name);
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

const reduceByBranchesSuccess = (state, payload) => {
  const repository = payload.repository;
  const response = payload.data;

  const key = createKey(repository);
  const repoState = state[key] || {};
  const byName = repoState.byName || {};
  repoState.byName = byName;

  const branches = response._embedded.branches;
  const names = branches.map(b => b.name);
  response._embedded.branches = names;
  for (let branch of branches) {
    byName[branch.name] = branch;
  }
  return {
    [key]: {
      list: response,
      byName
    }
  };
};

const reduceByBranchSuccess = (state, payload) => {
  const repository = payload.repository;
  const branch = payload.branch;

  const key = createKey(repository);

  const repoState = state[key] || {};

  const byName = repoState.byName || {};
  byName[branch.name] = branch;

  repoState.byName = byName;

  return {
    ...state,
    [key]: repoState
  };
};

export default function reducer(
  state: {} = {},
  action: Action = { type: "UNKNOWN" }
): Object {
  if (!action.payload) {
    return state;
  }
  const payload = action.payload;
  switch (action.type) {
    case FETCH_BRANCHES_SUCCESS:
      return reduceByBranchesSuccess(state, payload);
    case FETCH_BRANCH_SUCCESS:
      return reduceByBranchSuccess(state, payload);
    default:
      return state;
  }
}

function createKey(repository: Repository): string {
  const { namespace, name } = repository;
  return `${namespace}/${name}`;
}
