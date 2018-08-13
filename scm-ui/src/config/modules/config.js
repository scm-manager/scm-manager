// @flow
import { apiClient } from "../../apiclient";
import * as types from "../../modules/types";
import type { Action } from "../../types/Action";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";
import { Dispatch } from "redux";

export const FETCH_CONFIG = "scm/config/FETCH_CONFIG";
export const FETCH_CONFIG_PENDING = `${FETCH_CONFIG}_${types.PENDING_SUFFIX}`;
export const FETCH_CONFIG_SUCCESS = `${FETCH_CONFIG}_${types.SUCCESS_SUFFIX}`;
export const FETCH_CONFIG_FAILURE = `${FETCH_CONFIG}_${types.FAILURE_SUFFIX}`;

export const MODIFY_CONFIG = "scm/config/FETCH_CONFIG";
export const MODIFY_CONFIG_PENDING = `${MODIFY_CONFIG}_${types.PENDING_SUFFIX}`;
export const MODIFY_CONFIG_SUCCESS = `${MODIFY_CONFIG}_${types.SUCCESS_SUFFIX}`;
export const MODIFY_CONFIG_FAILURE = `${MODIFY_CONFIG}_${types.FAILURE_SUFFIX}`;

const CONFIG_URL = "config";
const CONTENT_TYPE_CONFIG = "application/vnd.scmm-config+json;v=2";

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

// modify config
export function modifyConfig(config: any, callback?: () => void) {
  return function(dispatch: Dispatch) {
    dispatch(modifyConfigPending(config));
    return apiClient
      .put(config._links.update.href, config, CONTENT_TYPE_CONFIG)
      .then(() => {
        dispatch(modifyConfigSuccess(config));
        if (callback) {
          callback();
        }
      })
      .catch(cause => {
        dispatch(
          modifyConfigFailure(
            config,
            new Error(`could not modify config: ${cause.message}`)
          )
        );
      });
  };
}

export function modifyConfigPending(config: any): Action {
  return {
    type: MODIFY_CONFIG_PENDING,
    payload: config
  };
}

export function modifyConfigSuccess(config: any): Action {
  return {
    type: MODIFY_CONFIG_SUCCESS,
    payload: config
  };
}

export function modifyConfigFailure(config: any, error: Error): Action {
  return {
    type: MODIFY_CONFIG_FAILURE,
    payload: {
      error,
      config
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

export function getConfig(state: Object) {
  if (state.config && state.config.entries) {
    return state.config.entries;
  }
}

export function getConfigUpdatePermission(state: Object) {
  if (state.config && state.config.configUpdatePermission) {
    return state.config.configUpdatePermission;
  }
}
