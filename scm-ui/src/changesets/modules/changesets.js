// @flow

import {FAILURE_SUFFIX, PENDING_SUFFIX, SUCCESS_SUFFIX} from "../../modules/types";
import {apiClient} from "@scm-manager/ui-components";
import {isPending} from "../../modules/pending";
import {getFailure} from "../../modules/failure";

export const FETCH_CHANGESETS = "scm/repos/FETCH_CHANGESETS";
export const FETCH_CHANGESETS_PENDING = `${FETCH_CHANGESETS}_${PENDING_SUFFIX}`;
export const FETCH_CHANGESETS_SUCCESS = `${FETCH_CHANGESETS}_${SUCCESS_SUFFIX}`;
export const FETCH_CHANGESETS_FAILURE = `${FETCH_CHANGESETS}_${FAILURE_SUFFIX}`;

const REPO_URL = "repositories";


// actions
export function fetchChangesetsByNamespaceAndName(namespace: string, name: string) {
  return function (dispatch: any) {
    dispatch(fetchChangesetsPending(namespace, name));
    return apiClient.get(REPO_URL + "/" + namespace + "/" + name + "/changesets").then(response => response.json())
      .then(data => {
        dispatch(fetchChangesetsSuccess(data, namespace, name))
      }).catch(cause => {
        dispatch(fetchChangesetsFailure(namespace, name, cause))
      })
  }
}

export function fetchChangesetsByNamespaceNameAndBranch(namespace: string, name: string, branch: string) {
  return function (dispatch: any) {
    dispatch(fetchChangesetsPending(namespace, name, branch));
    return apiClient.get(REPO_URL + "/" + namespace + "/" + name + "/branches/" + branch + "/changesets").then(response => response.json())
      .then(data => {
        dispatch(fetchChangesetsSuccess(data, namespace, name, branch))
      }).catch(cause => {
        dispatch(fetchChangesetsFailure(namespace, name, branch, cause))
      })
  }
}

export function fetchChangesetsPending(namespace: string, name: string, branch?: string): Action {
  return {
    type: FETCH_CHANGESETS_PENDING,
    payload: {
      namespace,
      name,
      branch
    },
    itemId: createItemId(namespace, name, branch)
  }
}

export function fetchChangesetsSuccess(collection: any, namespace: string, name: string, branch?: string): Action {
  return {
    type: FETCH_CHANGESETS_SUCCESS,
    payload: {collection, namespace, name, branch},
    itemId: createItemId(namespace, name, branch)
  }
}

function fetchChangesetsFailure(namespace: string, name: string, branch?: string, error: Error): Action {
  return {
    type: FETCH_CHANGESETS_FAILURE,
    payload: {
      namespace,
      name,
      branch,
      error
    },
    itemId: createItemId(namespace, name, branch)
  }
}

function createItemId(namespace: string, name: string, branch?: string): string {
  let itemId = namespace + "/" + name;
  if (branch && branch !== "") {
    itemId = itemId + "/" + branch;
  }
  return itemId;
}

// reducer
export default function reducer(state: any = {}, action: Action = {type: "UNKNOWN"}): Object {
  switch (action.type) {
    case FETCH_CHANGESETS_SUCCESS:
      const {namespace, name, branch} = action.payload;
      const key = createItemId(namespace, name, branch);
      let oldChangesets = {[key]: {}};
      if (state[key] !== undefined) {
        oldChangesets[key] = state[key]
      }
      return {...state, [key]: {byId: extractChangesetsByIds(action.payload.collection, oldChangesets[key].byId)}};
    default:
      return state;
  }
}

function extractChangesetsByIds(data: any, oldChangesetsByIds: any) {
  const changesets = data._embedded.changesets;
  const changesetsByIds = {};

  for (let changeset of changesets) {
    changesetsByIds[changeset.id] = changeset;
  }

  for (let id in oldChangesetsByIds) {
    changesetsByIds[id] = oldChangesetsByIds[id];
  }

  return changesetsByIds;
}

//selectors
export function getChangesets(state: Object, namespace: string, name: string, branch?: string) {
  const key = createItemId(namespace, name, branch);
  if (!state.changesets[key]) {
    return null;
  }
  return Object.values(state.changesets[key].byId);
}

export function isFetchChangesetsPending(state: Object, namespace: string, name: string, branch?: string) {
  return isPending(state, FETCH_CHANGESETS, createItemId(namespace, name, branch))
}

export function getFetchChangesetsFailure(state: Object, namespace: string, name: string, branch?: string) {
  return getFailure(state, FETCH_CHANGESETS, createItemId(namespace, name, branch));
}

