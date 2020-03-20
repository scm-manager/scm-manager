/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import { apiClient } from "@scm-manager/ui-components";
import * as types from "../../modules/types";
import { Action, Config } from "@scm-manager/ui-types";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";
import { Dispatch } from "redux";

export const FETCH_CONFIG = "scm/config/FETCH_CONFIG";
export const FETCH_CONFIG_PENDING = `${FETCH_CONFIG}_${types.PENDING_SUFFIX}`;
export const FETCH_CONFIG_SUCCESS = `${FETCH_CONFIG}_${types.SUCCESS_SUFFIX}`;
export const FETCH_CONFIG_FAILURE = `${FETCH_CONFIG}_${types.FAILURE_SUFFIX}`;

export const MODIFY_CONFIG = "scm/config/MODIFY_CONFIG";
export const MODIFY_CONFIG_PENDING = `${MODIFY_CONFIG}_${types.PENDING_SUFFIX}`;
export const MODIFY_CONFIG_SUCCESS = `${MODIFY_CONFIG}_${types.SUCCESS_SUFFIX}`;
export const MODIFY_CONFIG_FAILURE = `${MODIFY_CONFIG}_${types.FAILURE_SUFFIX}`;
export const MODIFY_CONFIG_RESET = `${MODIFY_CONFIG}_${types.RESET_SUFFIX}`;

const CONTENT_TYPE_CONFIG = "application/vnd.scmm-config+json;v=2";

//fetch config
export function fetchConfig(link: string) {
  return function(dispatch: any) {
    dispatch(fetchConfigPending());
    return apiClient
      .get(link)
      .then(response => {
        return response.json();
      })
      .then(data => {
        dispatch(fetchConfigSuccess(data));
      })
      .catch(err => {
        dispatch(fetchConfigFailure(err));
      });
  };
}

export function fetchConfigPending(): Action {
  return {
    type: FETCH_CONFIG_PENDING
  };
}

export function fetchConfigSuccess(config: Config): Action {
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
export function modifyConfig(config: Config, callback?: () => void) {
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
      .catch(err => {
        dispatch(modifyConfigFailure(config, err));
      });
  };
}

export function modifyConfigPending(config: Config): Action {
  return {
    type: MODIFY_CONFIG_PENDING,
    payload: config
  };
}

export function modifyConfigSuccess(config: Config): Action {
  return {
    type: MODIFY_CONFIG_SUCCESS,
    payload: config
  };
}

export function modifyConfigFailure(config: Config, error: Error): Action {
  return {
    type: MODIFY_CONFIG_FAILURE,
    payload: {
      error,
      config
    }
  };
}

export function modifyConfigReset() {
  return {
    type: MODIFY_CONFIG_RESET
  };
}

//reducer

function removeNullValues(config: Config) {
  if (!config.adminGroups) {
    config.adminGroups = [];
  }
  if (!config.adminUsers) {
    config.adminUsers = [];
  }
  if (!config.proxyExcludes) {
    config.proxyExcludes = [];
  }
  return config;
}

function reducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    case MODIFY_CONFIG_SUCCESS:
    case FETCH_CONFIG_SUCCESS:
      const config = removeNullValues(action.payload);
      return {
        ...state,
        entries: config,
        configUpdatePermission: action.payload._links.update ? true : false
      };
    default:
      return state;
  }
}

export default reducer;

// selectors

export function isFetchConfigPending(state: object) {
  return isPending(state, FETCH_CONFIG);
}

export function getFetchConfigFailure(state: object) {
  return getFailure(state, FETCH_CONFIG);
}

export function isModifyConfigPending(state: object) {
  return isPending(state, MODIFY_CONFIG);
}

export function getModifyConfigFailure(state: object) {
  return getFailure(state, MODIFY_CONFIG);
}

export function getConfig(state: object) {
  if (state.config && state.config.entries) {
    return state.config.entries;
  }
}

export function getConfigUpdatePermission(state: object) {
  if (state.config && state.config.configUpdatePermission) {
    return state.config.configUpdatePermission;
  }
}
