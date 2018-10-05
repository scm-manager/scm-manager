// @flow
import * as types from "./types";

import { apiClient } from "@scm-manager/ui-components";
import type { Action, IndexResources } from "@scm-manager/ui-types";
import { isPending } from "./pending";
import { getFailure } from "./failure";

// Action

export const FETCH_INDEXRESOURCES = "scm/INDEXRESOURCES";
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

// reducer
export default function reducer(
  state: Object = {},
  action: Action = { type: "UNKNOWN" }
): Object {
  if (!action.payload) {
    return state;
  }

  switch (action.type) {
    case FETCH_INDEXRESOURCES_SUCCESS:
      return {
        ...state,
        links: action.payload._links
      };
    default:
      return state;
  }
}

// selectors

export function isFetchIndexResourcesPending(state: Object) {
  return isPending(state, FETCH_INDEXRESOURCES);
}

export function getFetchIndexResourcesFailure(state: Object) {
  return getFailure(state, FETCH_INDEXRESOURCES);
}

export function getLinks(state: Object){
  return state.indexResources.links;
}

export function getUiPluginsLink(state: Object) {
  return state.indexResources.links["uiPlugins"].href;
}

export function getMeLink(state: Object) {
  if (state.indexResources.links["me"])
    return state.indexResources.links["me"].href;
  return undefined;
}

export function getLogoutLink(state: Object) {
  if (state.indexResources.links["logout"])
    return state.indexResources.links["logout"].href;
  return undefined;
}

export function getLoginLink(state: Object) {
  if (state.indexResources.links["login"])
    return state.indexResources.links["login"].href;
  return undefined;
}

export function getUsersLink(state: Object) {
  if (state.indexResources.links["users"])
    return state.indexResources.links["users"].href;
  return undefined;
}

export function getGroupsLink(state: Object) {
  if (state.indexResources.links["groups"])
    return state.indexResources.links["groups"].href;
  return undefined;
}

export function getConfigLink(state: Object) {
  if (state.indexResources.links["config"])
    return state.indexResources.links["config"].href;
  return undefined;
}

export function getRepositoriesLink(state: Object) {
  if (state.indexResources.links["repositories"])
    return state.indexResources.links["repositories"].href;
  return undefined;
}

export function getHgConfigLink(state: Object) {
  if (state.indexResources.links["hgConfig"])
    return state.indexResources.links["hgConfig"].href;
  return undefined;
}

export function getGitConfigLink(state: Object) {
  if (state.indexResources.links["gitConfig"])
    return state.indexResources.links["gitConfig"].href;
  return undefined;
}

export function getSvnConfigLink(state: Object) {
  if (state.indexResources.links["svnConfig"])
    return state.indexResources.links["svnConfig"].href;
  return undefined;
}
