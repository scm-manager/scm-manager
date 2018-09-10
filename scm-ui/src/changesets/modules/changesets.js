// @flow

import {FAILURE_SUFFIX, PENDING_SUFFIX, SUCCESS_SUFFIX} from "../../modules/types";
import { apiClient } from "@scm-manager/ui-components";

export const FETCH_CHANGESETS = "scm/repos/FETCH_CHANGESETS";
export const FETCH_CHANGESETS_PENDING = `${FETCH_CHANGESETS}_${PENDING_SUFFIX}`;
export const FETCH_CHANGESETS_SUCCESS = `${FETCH_CHANGESETS}_${SUCCESS_SUFFIX}`;
export const FETCH_CHANGESETS_FAILURE = `${FETCH_CHANGESETS}_${FAILURE_SUFFIX}`;

const REPO_URL = "repositories";

export function fetchChangesets(namespace: string, name: string) {
  return fetchChangesetsByLink(REPO_URL + "/" + namespace + "/" + name + "/changesets");
}

export function fetchChangesetsByLink(link: string) {
  return function(dispatch: any) {
    dispatch(fetchChangesetsPending());
    return apiClient.get(link).then(response => response.json())
      .then(data => {
        dispatch(fetchChangesetsSuccess(data))
      }).catch(cause => {
        dispatch(fetchChangesetsFailure(link, cause))
      })
  }
}

export function fetchChangesetsPending(): Action {
  return {
    type: FETCH_CHANGESETS_PENDING
  }
}

export function fetchChangesetsSuccess(data: any): Action {
  return {
    type: FETCH_CHANGESETS_SUCCESS,
    payload: data
  }
}

function fetchChangesetsFailure(url: string, error: Error): Action {
  return {
    type: FETCH_CHANGESETS_FAILURE,
    payload: {
      url,
      error
    }
  }
}


export default function reducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    case FETCH_CHANGESETS_SUCCESS:

      return {byIds: extractChangesetsByIds(action.payload)};
    default:
      return state;
  }
}

function extractChangesetsByIds(data: any) {
  const changesets = data._embedded.changesets;
  const changesetsByIds = {};
  for (let changeset of changesets) {
    changesetsByIds[changeset.id] = changeset;
  }

  return changesetsByIds;
}
