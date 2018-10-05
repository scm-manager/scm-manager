// @flow
import * as types from "./types";

import { apiClient } from "@scm-manager/ui-components";
import type { Action, IndexResources } from "@scm-manager/ui-types";

// Action

export const FETCH_INDEXRESOURCES = "scm/indexResource";
export const FETCH_INDEXRESOURCES_PENDING = `${FETCH_INDEXRESOURCES}_${
  types.PENDING_SUFFIX
}`;
export const FETCH_INDEXRESOURCES_SUCCESS = `${FETCH_INDEXRESOURCES}_${
  types.SUCCESS_SUFFIX
}`;
export const FETCH_INDEXRESOURCES_FAILURE = `${FETCH_INDEXRESOURCES}_${
  types.FAILURE_SUFFIX
}`;

const INDEX_RESOURCES_LINK = "/";

export function fetchIndexResources() {
  return function(dispatch: any) {
    dispatch(fetchIndexResourcesPending());
    return apiClient
      .get(INDEX_RESOURCES_LINK)
      .then(response => response.json())
      .then(resources => {
        dispatch(fetchIndexResourcesSuccess(resources));
      })
      .catch(err => {
        dispatch(fetchIndexResourcesFailure(err));
      });
  };
}

export function fetchIndexResourcesPending(): Action {
  return {
    type: FETCH_INDEXRESOURCES_PENDING
  };
}

export function fetchIndexResourcesSuccess(resources: IndexResources): Action {
  return {
    type: FETCH_INDEXRESOURCES_SUCCESS,
    payload: resources
  };
}

export function fetchIndexResourcesFailure(err: Error): Action {
  return {
    type: FETCH_INDEXRESOURCES_FAILURE,
    payload: err
  };
}
