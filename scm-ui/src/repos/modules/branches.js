// @flow
import {FAILURE_SUFFIX, PENDING_SUFFIX, SUCCESS_SUFFIX} from "../../modules/types";
import {apiClient} from "@scm-manager/ui-components";
import type {Repository} from "@scm-manager/ui-types";

export const FETCH_BRANCHES = "scm/repos/FETCH_BRANCHES";
export const FETCH_BRANCHES_PENDING = `${FETCH_BRANCHES}_${PENDING_SUFFIX}`;
export const FETCH_BRANCHES_SUCCESS = `${FETCH_BRANCHES}_${SUCCESS_SUFFIX}`;
export const FETCH_BRANCHES_FAILURE = `${FETCH_BRANCHES}_${FAILURE_SUFFIX}`;

// Fetching branches

export function fetchBranches(repository: Repository) {
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
// export function fetchBranchesByNamespaceAndName(
//   namespace: string,
//   name: string
// ) {
//   return function(dispatch: any) {
//     dispatch(fetchBranchesPending(namespace, name));
//     return apiClient
//       .get(REPO_URL + "/" + namespace + "/" + name + "/branches")
//       .then(response => response.json())
//       .then(data => {
//         dispatch(fetchBranchesSuccess(data, namespace, name));
//       })
//       .catch(error => {
//         dispatch(fetchBranchesFailure(namespace, name, error));
//       });
//   };
// }

// Action creators
export function fetchBranchesPending(repository: Repository) {
  const { namespace, name } = repository;
  return {
    type: FETCH_BRANCHES_PENDING,
    payload: { repository },
    itemId: namespace + "/" + name
  };
}

export function fetchBranchesSuccess(data: string, repository: Repository) {
  const { namespace, name } = repository;
  return {
    type: FETCH_BRANCHES_SUCCESS,
    payload: { data, repository },
    itemId: namespace + "/" + name
  };
}

export function fetchBranchesFailure(repository: Repository, error: Error) {
  const { namespace, name } = repository;
  return {
    type: FETCH_BRANCHES_FAILURE,
    payload: { error, repository },
    itemId: namespace + "/" + name
  };
}

// Reducers

export default function reducer(
  state: Object = {},
  action: Action = { type: "UNKNOWN" }
): Object {
  switch (action.type) {
    case FETCH_BRANCHES_SUCCESS:
      const { namespace, name } = action.payload.repository;
      const key = `${namespace}/${name}`;
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

function extractBranchesByNames(data: any, oldBranchesByNames: any): Branch[] {
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

export function getBranchesForNamespaceAndNameFromState(
  namespace: string,
  name: string,
  state: Object
) {
  const key = namespace + "/" + name;
  if (!state.branches[key]) {
    return null;
  }
  return Object.values(state.branches[key].byNames);
}

export function getBranchNames(state: Object, repository: Repository) {
  const { namespace, name } = repository;
  const key = namespace + "/" + name;
  if (!state.branches[key] || !state.branches[key].byNames) {
    return null;
  }
  return Object.keys(state.branches[key].byNames);
}
