// @flow

import {FAILURE_SUFFIX, PENDING_SUFFIX, SUCCESS_SUFFIX} from "../../modules/types";
import {apiClient} from "@scm-manager/ui-components";

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
        dispatch(fetchChangesetsFailure(link, cause))
      })
  }
}

export function fetchChangesetsPending(namespace: string, name: string): Action {
  return {
    type: FETCH_CHANGESETS_PENDING,
    payload: {
      namespace,
      name
    }
  }
}

export function fetchChangesetsSuccess(collection: any, namespace: string, name: string): Action {
  return {
    type: FETCH_CHANGESETS_SUCCESS,
    payload: {collection, namespace, name}
  }
}

function fetchChangesetsFailure(namespace: string, name: string, error: Error): Action {
  return {
    type: FETCH_CHANGESETS_FAILURE,
    payload: {
      namespace,
      name,
      error
    }
  }
}

// reducer
export default function reducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    case FETCH_CHANGESETS_SUCCESS:
      const {namespace, name} = action.payload;
      const key = namespace + "/" + name;

      let oldChangesets = {[key]: {}};
      if (state[key] !== undefined) {
        oldChangesets[key] = state[key]
      }
      return {[key]: {byId: extractChangesetsByIds(action.payload.collection, oldChangesets[key].byId)}};
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
export function getChangesetsForNameAndNamespaceFromState(namespace: string, name: string, state: any) {
  const key = namespace + "/" + name;
  if (!state.changesets[key]) {
    return null;
  }
  return Object.values(state.changesets[key].byId);
}
