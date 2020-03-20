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

import * as types from "../../modules/types";
import { Action, RepositoryType, RepositoryTypeCollection } from "@scm-manager/ui-types";
import { apiClient } from "@scm-manager/ui-components";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";

export const FETCH_REPOSITORY_TYPES = "scm/repos/FETCH_REPOSITORY_TYPES";
export const FETCH_REPOSITORY_TYPES_PENDING = `${FETCH_REPOSITORY_TYPES}_${types.PENDING_SUFFIX}`;
export const FETCH_REPOSITORY_TYPES_SUCCESS = `${FETCH_REPOSITORY_TYPES}_${types.SUCCESS_SUFFIX}`;
export const FETCH_REPOSITORY_TYPES_FAILURE = `${FETCH_REPOSITORY_TYPES}_${types.FAILURE_SUFFIX}`;

export function fetchRepositoryTypesIfNeeded() {
  return function(dispatch: any, getState: () => object) {
    if (shouldFetchRepositoryTypes(getState())) {
      return fetchRepositoryTypes(dispatch);
    }
  };
}

function fetchRepositoryTypes(dispatch: any) {
  dispatch(fetchRepositoryTypesPending());
  return apiClient
    .get("repositoryTypes")
    .then(response => response.json())
    .then(repositoryTypes => {
      dispatch(fetchRepositoryTypesSuccess(repositoryTypes));
    })
    .catch(error => {
      dispatch(fetchRepositoryTypesFailure(error));
    });
}

export function shouldFetchRepositoryTypes(state: object) {
  if (isFetchRepositoryTypesPending(state) || getFetchRepositoryTypesFailure(state)) {
    return false;
  }
  return !(state.repositoryTypes && state.repositoryTypes.length > 0);
}

export function fetchRepositoryTypesPending(): Action {
  return {
    type: FETCH_REPOSITORY_TYPES_PENDING
  };
}

export function fetchRepositoryTypesSuccess(repositoryTypes: RepositoryTypeCollection): Action {
  return {
    type: FETCH_REPOSITORY_TYPES_SUCCESS,
    payload: repositoryTypes
  };
}

export function fetchRepositoryTypesFailure(error: Error): Action {
  return {
    type: FETCH_REPOSITORY_TYPES_FAILURE,
    payload: error
  };
}

// reducers

export default function reducer(
  state: RepositoryType[] = [],
  action: Action = {
    type: "UNKNOWN"
  }
): RepositoryType[] {
  if (action.type === FETCH_REPOSITORY_TYPES_SUCCESS && action.payload) {
    return action.payload._embedded["repositoryTypes"];
  }
  return state;
}

// selectors

export function getRepositoryTypes(state: object) {
  if (state.repositoryTypes) {
    return state.repositoryTypes;
  }
  return [];
}

export function isFetchRepositoryTypesPending(state: object) {
  return isPending(state, FETCH_REPOSITORY_TYPES);
}

export function getFetchRepositoryTypesFailure(state: object) {
  return getFailure(state, FETCH_REPOSITORY_TYPES);
}
