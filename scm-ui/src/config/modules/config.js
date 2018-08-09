// @flow
import { apiClient } from "../../apiclient";
import * as types from "../../modules/types";
import type { Action } from "../../types/Action";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";

export const FETCH_CONFIG = "scm/groups/FETCH_CONFIG";
export const FETCH_CONFIG_PENDING = `${FETCH_CONFIG}_${types.PENDING_SUFFIX}`;
export const FETCH_CONFIG_SUCCESS = `${FETCH_CONFIG}_${types.SUCCESS_SUFFIX}`;
export const FETCH_CONFIG_FAILURE = `${FETCH_CONFIG}_${types.FAILURE_SUFFIX}`;

const CONFIG_URL = "config";

//fetch config
export function fetchConfig() {
  return function(dispatch: any) {
    dispatch(fetchConfigPending());
    return apiClient
      .get(CONFIG_URL)
      .then(response => {
        return response.json();
      })
      .then(data => {
        dispatch(fetchConfigSuccess(data));
      })
      .catch(cause => {
        const error = new Error(`could not fetch config: ${cause.message}`);
        dispatch(fetchConfigFailure(error));
      });
  };
}

export function fetchConfigPending(): Action {
  return {
    type: FETCH_CONFIG_PENDING
  };
}

export function fetchConfigSuccess(config: any): Action {
  return {
    type: FETCH_CONFIG_SUCCESS,
    payload: config
  };
}

export function fetchConfigFailure(error: Error): Action {
  return {
    type: FETCH_CONFIG_FAILURE,
    payload: {
      error
    }
  };
}

//reducer

function reducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    case FETCH_CONFIG_SUCCESS:
      return {
        ...state,
        entries: action.payload,
        configUpdatePermission: action.payload._links.update ? true : false
      };
    default:
      return state;
  }
}

export default reducer;

// selectors

export function isFetchConfigPending(state: Object) {
  return isPending(state, FETCH_CONFIG);
}

export function getFetchConfigFailure(state: Object) {
  return getFailure(state, FETCH_CONFIG);
}
