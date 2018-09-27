// @flow

import * as types from "../../../modules/types";
import type {
  Repository,
  SourcesCollection,
  Action
} from "@scm-manager/ui-types";
import { apiClient } from "@scm-manager/ui-components";
import { isPending } from "../../../modules/pending";
import { getFailure } from "../../../modules/failure";

export const FETCH_SOURCES = "scm/repos/FETCH_SOURCES";
export const FETCH_SOURCES_PENDING = `${FETCH_SOURCES}_${types.PENDING_SUFFIX}`;
export const FETCH_SOURCES_SUCCESS = `${FETCH_SOURCES}_${types.SUCCESS_SUFFIX}`;
export const FETCH_SOURCES_FAILURE = `${FETCH_SOURCES}_${types.FAILURE_SUFFIX}`;

export function fetchSources(repository: Repository) {
  return function(dispatch: any) {
    dispatch(fetchSourcesPending(repository));
    return apiClient
      .get(repository._links.sources.href)
      .then(response => response.json())
      .then(sources => {
        dispatch(fetchSourcesSuccess(repository, sources));
      })
      .catch(err => {
        const error = new Error(`failed to fetch sources: ${err.message}`);
        dispatch(fetchSourcesFailure(repository, error));
      });
  };
}

export function fetchSourcesPending(repository: Repository): Action {
  return {
    type: FETCH_SOURCES_PENDING,
    itemId: createItemId(repository)
  };
}

export function fetchSourcesSuccess(
  repository: Repository,
  sources: SourcesCollection
) {
  return {
    type: FETCH_SOURCES_SUCCESS,
    payload: sources,
    itemId: createItemId(repository)
  };
}

export function fetchSourcesFailure(
  repository: Repository,
  error: Error
): Action {
  return {
    type: FETCH_SOURCES_FAILURE,
    payload: error,
    itemId: createItemId(repository)
  };
}

function createItemId(repository: Repository) {
  return `${repository.namespace}/${repository.name}`;
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
  repository: Repository
): ?SourcesCollection {
  if (state.sources) {
    return state.sources[createItemId(repository)];
  }
  return null;
}

export function isFetchSourcesPending(
  state: any,
  repository: Repository
): boolean {
  return isPending(state, FETCH_SOURCES, createItemId(repository));
}

export function getFetchSourcesFailure(
  state: any,
  repository: Repository
): ?Error {
  return getFailure(state, FETCH_SOURCES, createItemId(repository));
}
