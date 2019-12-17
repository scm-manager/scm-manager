import * as types from "../../../modules/types";
import { Repository, File, Action, Link } from "@scm-manager/ui-types";
import { apiClient } from "@scm-manager/ui-components";
import { isPending } from "../../../modules/pending";
import { getFailure } from "../../../modules/failure";

export const FETCH_SOURCES = "scm/repos/FETCH_SOURCES";
export const FETCH_SOURCES_PENDING = `${FETCH_SOURCES}_${types.PENDING_SUFFIX}`;
export const FETCH_SOURCES_SUCCESS = `${FETCH_SOURCES}_${types.SUCCESS_SUFFIX}`;
export const FETCH_SOURCES_FAILURE = `${FETCH_SOURCES}_${types.FAILURE_SUFFIX}`;

export function fetchSources(repository: Repository, revision: string, path: string, initialLoad = true) {
  return function(dispatch: any, getState: () => any) {
    const state = getState();
    if (
      isFetchSourcesPending(state, repository, revision, path) ||
      isUpdateSourcePending(state, repository, revision, path)
    ) {
      return;
    }

    if (initialLoad) {
      dispatch(fetchSourcesPending(repository, revision, path));
    } else {
      dispatch(updateSourcesPending(repository, revision, path, getSources(state, repository, revision, path)));
    }
    return apiClient
      .get(createUrl(repository, revision, path))
      .then(response => response.json())
      .then((sources: File) => {
        dispatch(fetchSourcesSuccess(repository, revision, path, sources));
      })
      .catch(err => {
        dispatch(fetchSourcesFailure(repository, revision, path, err));
      });
  };
}

function createUrl(repository: Repository, revision: string, path: string) {
  const base = (repository._links.sources as Link).href;
  if (!revision && !path) {
    return base;
  }

  // TODO handle trailing slash
  const pathDefined = path ? path : "";
  return `${base}${encodeURIComponent(revision)}/${pathDefined}`;
}

export function fetchSourcesPending(repository: Repository, revision: string, path: string): Action {
  return {
    type: FETCH_SOURCES_PENDING,
    itemId: createItemId(repository, revision, path)
  };
}

export function updateSourcesPending(
  repository: Repository,
  revision: string,
  path: string,
  currentSources: any
): Action {
  return {
    type: "UPDATE_PENDING",
    payload: { updatePending: true, sources: currentSources },
    itemId: createItemId(repository, revision, path)
  };
}

export function fetchSourcesSuccess(repository: Repository, revision: string, path: string, sources: File) {
  return {
    type: FETCH_SOURCES_SUCCESS,
    payload: { updatePending: false, sources },
    itemId: createItemId(repository, revision, path)
  };
}

export function fetchSourcesFailure(repository: Repository, revision: string, path: string, error: Error): Action {
  return {
    type: FETCH_SOURCES_FAILURE,
    payload: error,
    itemId: createItemId(repository, revision, path)
  };
}

function createItemId(repository: Repository, revision: string, path: string) {
  const revPart = revision ? revision : "_";
  const pathPart = path ? path : "";
  return `${repository.namespace}/${repository.name}/${decodeURIComponent(revPart)}/${pathPart}`;
}

// reducer

export default function reducer(
  state: any = {},
  action: Action = {
    type: "UNKNOWN"
  }
): any {
  if (action.itemId && (action.type === FETCH_SOURCES_SUCCESS || action.type === "UPDATE_PENDING")) {
    return {
      ...state,
      [action.itemId]: action.payload
    };
  }
  return state;
}

// selectors

export function isDirectory(state: any, repository: Repository, revision: string, path: string): boolean {
  const currentFile = getSources(state, repository, revision, path);
  if (currentFile && !currentFile.directory) {
    return false;
  } else {
    return true; //also return true if no currentFile is found since it is the "default" path
  }
}

export function getSources(
  state: any,
  repository: Repository,
  revision: string,
  path: string
): File | null | undefined {
  if (state.sources) {
    return state.sources[createItemId(repository, revision, path)]?.sources;
  }
  return null;
}

export function isFetchSourcesPending(state: any, repository: Repository, revision: string, path: string): boolean {
  return state && isPending(state, FETCH_SOURCES, createItemId(repository, revision, path));
}

function isUpdateSourcePending(state: any, repository: Repository, revision: string, path: string): boolean {
  return state?.sources[createItemId(repository, revision, path)]?.updatePending;
}

export function getFetchSourcesFailure(
  state: any,
  repository: Repository,
  revision: string,
  path: string
): Error | null | undefined {
  return getFailure(state, FETCH_SOURCES, createItemId(repository, revision, path));
}
