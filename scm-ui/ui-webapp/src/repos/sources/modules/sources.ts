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

import * as types from "../../../modules/types";
import { Action, File, Link, Repository } from "@scm-manager/ui-types";
import { apiClient } from "@scm-manager/ui-components";
import { getFailure } from "../../../modules/failure";

export const FETCH_SOURCES = "scm/repos/FETCH_SOURCES";
export const FETCH_SOURCES_PENDING = `${FETCH_SOURCES}_${types.PENDING_SUFFIX}`;
export const FETCH_UPDATES_PENDING = `${FETCH_SOURCES}_UPDATE_PENDING`;
export const FETCH_SOURCES_SUCCESS = `${FETCH_SOURCES}_${types.SUCCESS_SUFFIX}`;
export const FETCH_UPDATES_SUCCESS = `${FETCH_SOURCES}_UPDATE_SUCCESS`;
export const FETCH_SOURCES_FAILURE = `${FETCH_SOURCES}_${types.FAILURE_SUFFIX}`;

export function fetchSources(repository: Repository, revision: string, path: string, initialLoad = true, hunk = 0) {
  return function(dispatch: any, getState: () => any) {
    const state = getState();
    if (
      isFetchSourcesPending(state, repository, revision, path, hunk) ||
      isUpdateSourcePending(state, repository, revision, path, hunk)
    ) {
      return;
    }

    if (initialLoad) {
      dispatch(fetchSourcesPending(repository, revision, path, hunk));
    } else {
      dispatch(
        updateSourcesPending(repository, revision, path, hunk, getSources(state, repository, revision, path, hunk))
      );
    }

    let offset = 0;
    for (let i = 0; i < hunk; ++i) {
      const sources = getSources(state, repository, revision, path, i);
      if (sources?._embedded.children) {
        offset += sources._embedded.children.filter(c => !c.directory).length;
      }
    }

    return apiClient
      .get(createUrl(repository, revision, path, offset))
      .then(response => response.json())
      .then((sources: File) => {
        if (initialLoad) {
          dispatch(fetchSourcesSuccess(repository, revision, path, hunk, sources));
        } else {
          dispatch(fetchUpdatesSuccess(repository, revision, path, hunk, sources));
        }
      })
      .catch(err => {
        dispatch(fetchSourcesFailure(repository, revision, path, hunk, err));
      });
  };
}

function createUrl(repository: Repository, revision: string, path: string, offset: number) {
  const base = (repository._links.sources as Link).href;
  if (!revision && !path) {
    return `${base}?offset=${offset}`;
  }

  // TODO handle trailing slash
  const pathDefined = path ? path : "";
  return `${base}${encodeURIComponent(revision)}/${pathDefined}?offset=${offset}`;
}

export function fetchSourcesPending(repository: Repository, revision: string, path: string, hunk: number): Action {
  return {
    type: FETCH_SOURCES_PENDING,
    itemId: createItemId(repository, revision, path, ""),
    payload: { hunk, pending: true, updatePending: false, sources: {} }
  };
}

export function updateSourcesPending(
  repository: Repository,
  revision: string,
  path: string,
  hunk: number,
  currentSources: File
): Action {
  return {
    type: FETCH_UPDATES_PENDING,
    payload: { hunk, pending: false, updatePending: true, sources: currentSources },
    itemId: createItemId(repository, revision, path, "")
  };
}

export function fetchSourcesSuccess(
  repository: Repository,
  revision: string,
  path: string,
  hunk: number,
  sources: File
) {
  return {
    type: FETCH_SOURCES_SUCCESS,
    payload: { hunk, pending: false, updatePending: false, sources },
    itemId: createItemId(repository, revision, path, "")
  };
}

export function fetchUpdatesSuccess(
  repository: Repository,
  revision: string,
  path: string,
  hunk: number,
  sources: File
) {
  return {
    type: FETCH_UPDATES_SUCCESS,
    payload: { hunk, pending: false, updatePending: false, sources },
    itemId: createItemId(repository, revision, path, "")
  };
}

export function fetchSourcesFailure(
  repository: Repository,
  revision: string,
  path: string,
  hunk: number,
  error: Error
): Action {
  return {
    type: FETCH_SOURCES_FAILURE,
    payload: { hunk, pending: false, updatePending: false, error },
    itemId: createItemId(repository, revision, path, "")
  };
}

function createItemId(repository: Repository, revision: string | undefined, path: string, hunk: number | string) {
  const revPart = revision ? revision : "_";
  const pathPart = path ? path : "";
  return `${repository.namespace}/${repository.name}/${decodeURIComponent(revPart)}/${pathPart}/${hunk}`;
}

// reducer

export default function reducer(
  state: any = {},
  action: Action = {
    type: "UNKNOWN"
  }
): any {
  if (action.itemId && (action.type === FETCH_SOURCES_SUCCESS || action.type === FETCH_SOURCES_FAILURE)) {
    return {
      ...state,
      [action.itemId + "hunkCount"]: action.payload.hunk + 1,
      [action.itemId + action.payload.hunk]: {
        sources: action.payload.sources,
        error: action.payload.error,
        updatePending: false,
        pending: false
      }
    };
  } else if (action.itemId && (action.type === FETCH_UPDATES_SUCCESS)) {
    return {
      ...state,
      [action.itemId + action.payload.hunk]: {
        sources: action.payload.sources,
        error: action.payload.error,
        updatePending: false,
        pending: false
      }
    };
  } else if (action.itemId && action.type === FETCH_UPDATES_PENDING) {
    return {
      ...state,
      [action.itemId + action.payload.hunk]: {
        sources: action.payload.sources,
        updatePending: true,
        pending: false
      }
    };
  } else if (action.itemId && action.type === FETCH_SOURCES_PENDING) {
    return {
      ...state,
      [action.itemId + "hunkCount"]: action.payload.hunk + 1,
      [action.itemId + action.payload.hunk]: {
        updatePending: false,
        pending: true
      }
    };
  } else {
    return state;
  }
}

// selectors

export function isDirectory(state: any, repository: Repository, revision: string, path: string): boolean {
  const currentFile = getSources(state, repository, revision, path, 0);
  if (currentFile && !currentFile.directory) {
    return false;
  } else {
    return true; //also return true if no currentFile is found since it is the "default" path
  }
}

export function getHunkCount(state: any, repository: Repository, revision: string | undefined, path: string): number {
  if (state.sources) {
    const count = state.sources[createItemId(repository, revision, path, "hunkCount")];
    return count ? count : 0;
  }
  return 0;
}

export function getSources(
  state: any,
  repository: Repository,
  revision: string | undefined,
  path: string,
  hunk = 0
): File | null | undefined {
  if (state.sources) {
    return state.sources[createItemId(repository, revision, path, hunk)]?.sources;
  }
  return null;
}

export function isFetchSourcesPending(
  state: any,
  repository: Repository,
  revision: string,
  path: string,
  hunk = 0
): boolean {
  if (state.sources) {
    return state.sources[createItemId(repository, revision, path, hunk)]?.pending;
  }
  return false;
}

export function isUpdateSourcePending(
  state: any,
  repository: Repository,
  revision: string,
  path: string,
  hunk: number
): boolean {
  if (state.sources) {
    return state.sources[createItemId(repository, revision, path, hunk)]?.updatePending;
  }
  return false;
}

export function getFetchSourcesFailure(
  state: any,
  repository: Repository,
  revision: string,
  path: string,
  hunk = 0
): Error | null | undefined {
  if (state.sources) {
    return state.sources && state.sources[createItemId(repository, revision, path, hunk)]?.error;
  }
  return null;
}
