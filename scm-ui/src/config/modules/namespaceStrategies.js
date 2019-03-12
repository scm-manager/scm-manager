// @flow

import * as types from "../../modules/types";
import type { Action, NamespaceStrategies } from "@scm-manager/ui-types";
import { apiClient } from "@scm-manager/ui-components";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";
import { MODIFY_CONFIG_SUCCESS } from "./config";

export const FETCH_NAMESPACESTRATEGIES_TYPES =
  "scm/config/FETCH_NAMESPACESTRATEGIES_TYPES";
export const FETCH_NAMESPACESTRATEGIES_TYPES_PENDING = `${FETCH_NAMESPACESTRATEGIES_TYPES}_${
  types.PENDING_SUFFIX
}`;
export const FETCH_NAMESPACESTRATEGIES_TYPES_SUCCESS = `${FETCH_NAMESPACESTRATEGIES_TYPES}_${
  types.SUCCESS_SUFFIX
}`;
export const FETCH_NAMESPACESTRATEGIES_TYPES_FAILURE = `${FETCH_NAMESPACESTRATEGIES_TYPES}_${
  types.FAILURE_SUFFIX
}`;

export function fetchNamespaceStrategiesIfNeeded() {
  return function(dispatch: any, getState: () => Object) {
    const state = getState();
    if (shouldFetchNamespaceStrategies(state)) {
      return fetchNamespaceStrategies(
        dispatch,
        state.indexResources.links.namespaceStrategies.href
      );
    }
  };
}

function fetchNamespaceStrategies(dispatch: any, url: string) {
  dispatch(fetchNamespaceStrategiesPending());
  return apiClient
    .get(url)
    .then(response => response.json())
    .then(namespaceStrategies => {
      dispatch(fetchNamespaceStrategiesSuccess(namespaceStrategies));
    })
    .catch(error => {
      dispatch(fetchNamespaceStrategiesFailure(error));
    });
}

export function shouldFetchNamespaceStrategies(state: Object) {
  if (
    isFetchNamespaceStrategiesPending(state) ||
    getFetchNamespaceStrategiesFailure(state)
  ) {
    return false;
  }
  return !state.namespaceStrategies || !state.namespaceStrategies.current;
}

export function fetchNamespaceStrategiesPending(): Action {
  return {
    type: FETCH_NAMESPACESTRATEGIES_TYPES_PENDING
  };
}

export function fetchNamespaceStrategiesSuccess(
  namespaceStrategies: NamespaceStrategies
): Action {
  return {
    type: FETCH_NAMESPACESTRATEGIES_TYPES_SUCCESS,
    payload: namespaceStrategies
  };
}

export function fetchNamespaceStrategiesFailure(error: Error): Action {
  return {
    type: FETCH_NAMESPACESTRATEGIES_TYPES_FAILURE,
    payload: error
  };
}

// reducers

export default function reducer(
  state: Object = {},
  action: Action = { type: "UNKNOWN" }
): Object {
  if (
    action.type === FETCH_NAMESPACESTRATEGIES_TYPES_SUCCESS &&
    action.payload
  ) {
    return action.payload;
  } else if (action.type === MODIFY_CONFIG_SUCCESS && action.payload) {
    const config = action.payload;
    return {
      ...state,
      current: config.namespaceStrategy
    };
  }
  return state;
}

// selectors

export function getNamespaceStrategies(state: Object) {
  if (state.namespaceStrategies) {
    return state.namespaceStrategies;
  }
  return {};
}

export function isFetchNamespaceStrategiesPending(state: Object) {
  return isPending(state, FETCH_NAMESPACESTRATEGIES_TYPES);
}

export function getFetchNamespaceStrategiesFailure(state: Object) {
  return getFailure(state, FETCH_NAMESPACESTRATEGIES_TYPES);
}
