import {FAILURE_SUFFIX, PENDING_SUFFIX, SUCCESS_SUFFIX} from "../../modules/types";
import {apiClient} from "@scm-manager/ui-components";

export const FETCH_BRANCHES = "scm/repos/FETCH_BRANCHES";
export const FETCH_BRANCHES_PENDING = `${FETCH_BRANCHES}_${PENDING_SUFFIX}`;
export const FETCH_BRANCHES_SUCCESS = `${FETCH_BRANCHES}_${SUCCESS_SUFFIX}`;
export const FETCH_BRANCHES_FAILURE = `${FETCH_BRANCHES}_${FAILURE_SUFFIX}`;

const REPO_URL = "repositories";

// Fetching branches
export function fetchBranchesByNamespaceAndName(namespace: string, name: string) {
  return function (dispatch: any) {
    dispatch(fetchBranchesPending(namespace, name));
    return apiClient.get(REPO_URL + "/" + namespace + "/" + name + "/branches")
      .then(response => response.json())
      .then(data => {
        dispatch(fetchBranchesSuccess(data, namespace, name))
      })
      .catch(cause => {
        dispatch(fetchBranchesFailure(namespace, name, cause))
      })
  }
}

// Action creators
export function fetchBranchesPending(namespace: string, name: string) {
  return {
    type: FETCH_BRANCHES_PENDING,
    payload: {namespace, name},
    itemId: namespace + "/" + name
  }
}

export function fetchBranchesSuccess(data: string, namespace: string, name: string) {
  return {
    type: FETCH_BRANCHES_SUCCESS,
    payload: {data, namespace, name},
    itemId: namespace + "/" + name
  }
}

export function fetchBranchesFailure(namespace: string, name: string, error: Error) {
  return {
    type: FETCH_BRANCHES_FAILURE,
    payload: {error, namespace, name},
    itemId: namespace + "/" + name
  }
}

// Reducers

// Selectors
