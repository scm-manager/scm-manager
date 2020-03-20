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
///
/// MIT License
///
/// Copyright (c) 2020-present Cloudogu GmbH and Contributors
///
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
///
/// The above copyright notice and this permission notice shall be included in all
/// copies or substantial portions of the Software.
///
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
/// SOFTWARE.
///

import * as types from "../../modules/types";
import { Action, NamespaceStrategies } from "@scm-manager/ui-types";
import { apiClient } from "@scm-manager/ui-components";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";
import { MODIFY_CONFIG_SUCCESS } from "./config";

export const FETCH_NAMESPACESTRATEGIES_TYPES = "scm/config/FETCH_NAMESPACESTRATEGIES_TYPES";
export const FETCH_NAMESPACESTRATEGIES_TYPES_PENDING = `${FETCH_NAMESPACESTRATEGIES_TYPES}_${types.PENDING_SUFFIX}`;
export const FETCH_NAMESPACESTRATEGIES_TYPES_SUCCESS = `${FETCH_NAMESPACESTRATEGIES_TYPES}_${types.SUCCESS_SUFFIX}`;
export const FETCH_NAMESPACESTRATEGIES_TYPES_FAILURE = `${FETCH_NAMESPACESTRATEGIES_TYPES}_${types.FAILURE_SUFFIX}`;

export function fetchNamespaceStrategiesIfNeeded() {
  return function(dispatch: any, getState: () => object) {
    const state = getState();
    if (shouldFetchNamespaceStrategies(state)) {
      return fetchNamespaceStrategies(dispatch, state.indexResources.links.namespaceStrategies.href);
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

export function shouldFetchNamespaceStrategies(state: object) {
  if (isFetchNamespaceStrategiesPending(state) || getFetchNamespaceStrategiesFailure(state)) {
    return false;
  }
  return !state.namespaceStrategies || !state.namespaceStrategies.current;
}

export function fetchNamespaceStrategiesPending(): Action {
  return {
    type: FETCH_NAMESPACESTRATEGIES_TYPES_PENDING
  };
}

export function fetchNamespaceStrategiesSuccess(namespaceStrategies: NamespaceStrategies): Action {
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
  state: object = {},
  action: Action = {
    type: "UNKNOWN"
  }
): object {
  if (action.type === FETCH_NAMESPACESTRATEGIES_TYPES_SUCCESS && action.payload) {
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

export function getNamespaceStrategies(state: object) {
  if (state.namespaceStrategies) {
    return state.namespaceStrategies;
  }
  return {};
}

export function isFetchNamespaceStrategiesPending(state: object) {
  return isPending(state, FETCH_NAMESPACESTRATEGIES_TYPES);
}

export function getFetchNamespaceStrategiesFailure(state: object) {
  return getFailure(state, FETCH_NAMESPACESTRATEGIES_TYPES);
}
