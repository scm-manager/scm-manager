// @flow

import * as types from "../../../modules/types";
import type { Repository, File, Action } from "@scm-manager/ui-types";
import { apiClient } from "@scm-manager/ui-components";
import { isPending } from "../../../modules/pending";
import { getFailure } from "../../../modules/failure";

export const FETCH_SOURCES = "scm/repos/FETCH_SOURCES";
export const FETCH_SOURCES_PENDING = `${FETCH_SOURCES}_${types.PENDING_SUFFIX}`;
export const FETCH_SOURCES_SUCCESS = `${FETCH_SOURCES}_${types.SUCCESS_SUFFIX}`;
export const FETCH_SOURCES_FAILURE = `${FETCH_SOURCES}_${types.FAILURE_SUFFIX}`;

export function fetchSources(
  repository: Repository,
  revision: string,
  path: string
) {
  return function(dispatch: any) {
    dispatch(fetchSourcesPending(repository, revision, path));
    return apiClient
      .get(createUrl(repository, revision, path))
      .then(response => response.json())
      .then(sources => {
        dispatch(fetchSourcesSuccess(repository, revision, path, sources));
      })
      .catch(err => {
        const error = new Error(`failed to fetch sources: ${err.message}`);
        dispatch(fetchSourcesFailure(repository, revision, path, error));
      });
  };
}

function createUrl(repository: Repository, revision: string, path: string) {
  const base = repository._links.sources.href;
  if (!revision && !path) {
    return base;
  }

  // TODO handle trailing slash
  const pathDefined = path ? path : "";
  return `${base}${encodeURIComponent(revision)}/${pathDefined}`;
}

export function fetchSourcesPending(
  repository: Repository,
  revision: string,
  path: string
): Action {
  return {
    type: FETCH_SOURCES_PENDING,
    itemId: createItemId(repository, revision, path)
  };
}

export function fetchSourcesSuccess(
  repository: Repository,
  revision: string,
  path: string,
  sources: File
) {
  return {
    type: FETCH_SOURCES_SUCCESS,
    payload: sources,
    itemId: createItemId(repository, revision, path)
  };
}

export function fetchSourcesFailure(
  repository: Repository,
  revision: string,
  path: string,
  error: Error
): Action {
  return {
    type: FETCH_SOURCES_FAILURE,
    payload: error,
    itemId: createItemId(repository, revision, path)
  };
}

function createItemId(repository: Repository, revision: string, path: string) {
  const revPart = revision ? revision : "_";
  const pathPart = path ? path : "";
  return `${repository.namespace}/${repository.name}/${revPart}/${pathPart}`;
}

// reducer

export default function reducer(
  state: any = {},
  action: Action = { type: "UNKNOWN" }
): any {
  if (action.type === FETCH_SOURCES_SUCCESS) {
    return {
      [action.itemId]: action.payload,
      ...state
    };
  }
  return state;
}

// selectors

export function getSources(
  state: any,
  repository: Repository,
  revision: string,
  path: string
): ?File {
  if (state.sources) {
    return state.sources[createItemId(repository, revision, path)];
  }
  return null;
}

export function isFetchSourcesPending(
  state: any,
  repository: Repository,
  revision: string,
  path: string
): boolean {
  return isPending(
    state,
    FETCH_SOURCES,
    createItemId(repository, revision, path)
  );
}

export function getFetchSourcesFailure(
  state: any,
  repository: Repository,
  revision: string,
  path: string
): ?Error {
  return getFailure(
    state,
    FETCH_SOURCES,
    createItemId(repository, revision, path)
  );
}
