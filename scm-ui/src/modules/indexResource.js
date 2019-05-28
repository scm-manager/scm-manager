// @flow
import * as types from "./types";

import { apiClient } from "@scm-manager/ui-components";
import type { Action, IndexResources, Link } from "@scm-manager/ui-types";
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

export const callFetchIndexResources = (): Promise<IndexResources> => {
  return apiClient.get(INDEX_RESOURCES_LINK).then(response => {
    return response.json();
  });
};

export function fetchIndexResources() {
  return function(dispatch: any) {
    dispatch(fetchIndexResourcesPending());
    return callFetchIndexResources()
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

export function getLinks(state: Object) {
  return state.indexResources.links;
}

export function getLink(state: Object, name: string) {
  if (state.indexResources.links && state.indexResources.links[name]) {
    return state.indexResources.links[name].href;
  }
}

export function getLinkCollection(state: Object, name: string): Link[] {
  if (state.indexResources.links && state.indexResources.links[name]) {
    return state.indexResources.links[name];
  }
  return [];
}

export function getUiPluginsLink(state: Object) {
  return getLink(state, "uiPlugins");
}

export function getMeLink(state: Object) {
  return getLink(state, "me");
}

export function getLogoutLink(state: Object) {
  return getLink(state, "logout");
}

export function getLoginLink(state: Object) {
  return getLink(state, "login");
}

export function getUsersLink(state: Object) {
  return getLink(state, "users");
}

export function getRepositoryRolesLink(state: Object) {
  return getLink(state, "repositoryRoles");
}

export function getRepositoryVerbsLink(state: Object) {
  return getLink(state, "repositoryVerbs");
}

export function getGroupsLink(state: Object) {
  return getLink(state, "groups");
}

export function getConfigLink(state: Object) {
  return getLink(state, "config");
}

export function getRepositoriesLink(state: Object) {
  return getLink(state, "repositories");
}

export function getHgConfigLink(state: Object) {
  return getLink(state, "hgConfig");
}

export function getGitConfigLink(state: Object) {
  return getLink(state, "gitConfig");
}

export function getSvnConfigLink(state: Object) {
  return getLink(state, "svnConfig");
}

export function getUserAutoCompleteLink(state: Object): string {
  const link = getLinkCollection(state, "autocomplete").find(
    i => i.name === "users"
  );
  if (link) {
    return link.href;
  }
  return "";
}

export function getGroupAutoCompleteLink(state: Object): string {
  const link = getLinkCollection(state, "autocomplete").find(
    i => i.name === "groups"
  );
  if (link) {
    return link.href;
  }
  return "";
}
