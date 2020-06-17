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

import { FAILURE_SUFFIX, PENDING_SUFFIX, SUCCESS_SUFFIX } from "../../modules/types";
import { apiClient, urls } from "@scm-manager/ui-components";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";
import { Action, Branch, PagedCollection, Repository } from "@scm-manager/ui-types";
import memoizeOne from "memoize-one";

export const FETCH_CHANGESETS = "scm/repos/FETCH_CHANGESETS";
export const FETCH_CHANGESETS_PENDING = `${FETCH_CHANGESETS}_${PENDING_SUFFIX}`;
export const FETCH_CHANGESETS_SUCCESS = `${FETCH_CHANGESETS}_${SUCCESS_SUFFIX}`;
export const FETCH_CHANGESETS_FAILURE = `${FETCH_CHANGESETS}_${FAILURE_SUFFIX}`;

export const FETCH_CHANGESET = "scm/repos/FETCH_CHANGESET";
export const FETCH_CHANGESET_PENDING = `${FETCH_CHANGESET}_${PENDING_SUFFIX}`;
export const FETCH_CHANGESET_SUCCESS = `${FETCH_CHANGESET}_${SUCCESS_SUFFIX}`;
export const FETCH_CHANGESET_FAILURE = `${FETCH_CHANGESET}_${FAILURE_SUFFIX}`;

// actions
//TODO: Content type

export function fetchChangesetIfNeeded(repository: Repository, id: string) {
  return (dispatch: any, getState: any) => {
    if (shouldFetchChangeset(getState(), repository, id)) {
      return dispatch(fetchChangeset(repository, id));
    }
  };
}

export function fetchChangeset(repository: Repository, id: string) {
  return function(dispatch: any) {
    dispatch(fetchChangesetPending(repository, id));
    return apiClient
      .get(createChangesetUrl(repository, id))
      .then(response => response.json())
      .then(data => dispatch(fetchChangesetSuccess(data, repository, id)))
      .catch(err => {
        dispatch(fetchChangesetFailure(repository, id, err));
      });
  };
}

function createChangesetUrl(repository: Repository, id: string) {
  return urls.concat(repository._links.changesets.href, id);
}

export function fetchChangesetPending(repository: Repository, id: string): Action {
  return {
    type: FETCH_CHANGESET_PENDING,
    itemId: createChangesetItemId(repository, id)
  };
}

export function fetchChangesetSuccess(changeset: any, repository: Repository, id: string): Action {
  return {
    type: FETCH_CHANGESET_SUCCESS,
    payload: {
      changeset,
      repository,
      id
    },
    itemId: createChangesetItemId(repository, id)
  };
}

function fetchChangesetFailure(repository: Repository, id: string, error: Error): Action {
  return {
    type: FETCH_CHANGESET_FAILURE,
    payload: {
      repository,
      id,
      error
    },
    itemId: createChangesetItemId(repository, id)
  };
}

export function fetchChangesets(repository: Repository, branch?: Branch, page?: number) {
  const link = createChangesetsLink(repository, branch, page);

  return function(dispatch: any) {
    dispatch(fetchChangesetsPending(repository, branch));
    return apiClient
      .get(link)
      .then(response => response.json())
      .then(data => {
        dispatch(fetchChangesetsSuccess(repository, branch, data));
      })
      .catch(cause => {
        dispatch(fetchChangesetsFailure(repository, branch, cause));
      });
  };
}

function createChangesetsLink(repository: Repository, branch?: Branch, page?: number) {
  let link = repository._links.changesets.href;

  if (branch) {
    link = branch._links.history.href;
  }

  if (page) {
    link = link + `?page=${page - 1}`;
  }
  return link;
}

export function fetchChangesetsPending(repository: Repository, branch?: Branch): Action {
  const itemId = createItemId(repository, branch);

  return {
    type: FETCH_CHANGESETS_PENDING,
    itemId
  };
}

export function fetchChangesetsSuccess(repository: Repository, branch?: Branch, changesets: any): Action {
  return {
    type: FETCH_CHANGESETS_SUCCESS,
    payload: {
      repository,
      branch,
      changesets
    },
    itemId: createItemId(repository, branch)
  };
}

function fetchChangesetsFailure(repository: Repository, branch?: Branch, error: Error): Action {
  return {
    type: FETCH_CHANGESETS_FAILURE,
    payload: {
      repository,
      error,
      branch
    },
    itemId: createItemId(repository, branch)
  };
}

function createChangesetItemId(repository: Repository, id: string) {
  const { namespace, name } = repository;
  return namespace + "/" + name + "/" + id;
}

function createItemId(repository: Repository, branch?: Branch): string {
  const { namespace, name } = repository;
  let itemId = namespace + "/" + name;
  if (branch) {
    itemId = itemId + "/" + branch.name;
  }
  return itemId;
}

// reducer
export default function reducer(
  state: any = {},
  action: Action = {
    type: "UNKNOWN"
  }
): object {
  if (!action.payload) {
    return state;
  }

  const payload = action.payload;
  switch (action.type) {
    case FETCH_CHANGESET_SUCCESS:
      const _key = createItemId(payload.repository);

      let _oldByIds = {};
      if (state[_key] && state[_key].byId) {
        _oldByIds = state[_key].byId;
      }

      const changeset = payload.changeset;

      return {
        ...state,
        [_key]: {
          ...state[_key],
          byId: {
            ..._oldByIds,
            [payload.id]: changeset
          }
        }
      };

    case FETCH_CHANGESETS_SUCCESS:
      const changesets = payload.changesets._embedded.changesets;
      const changesetIds = changesets.map(c => c.id);
      const key = action.itemId;

      if (!key) {
        return state;
      }

      const repoId = createItemId(payload.repository);

      let oldState = {};
      if (state[repoId]) {
        oldState = state[repoId];
      }

      const branchName = payload.branch ? payload.branch.name : "";
      const byIds = extractChangesetsByIds(changesets);

      return {
        ...state,
        [repoId]: {
          byId: {
            ...oldState.byId,
            ...byIds
          },
          byBranch: {
            ...oldState.byBranch,
            [branchName]: {
              entries: changesetIds,
              entry: {
                page: payload.changesets.page,
                pageTotal: payload.changesets.pageTotal,
                _links: payload.changesets._links
              }
            }
          }
        }
      };
    default:
      return state;
  }
}

function extractChangesetsByIds(changesets: any) {
  const changesetsByIds = {};

  for (const changeset of changesets) {
    changesetsByIds[changeset.id] = changeset;
  }

  return changesetsByIds;
}

//selectors
export function getChangesets(state: object, repository: Repository, branch?: Branch) {
  const repoKey = createItemId(repository);

  const stateRoot = state.changesets[repoKey];
  if (!stateRoot || !stateRoot.byBranch) {
    return null;
  }

  const branchName = branch ? branch.name : "";

  const changesets = stateRoot.byBranch[branchName];
  if (!changesets) {
    return null;
  }

  return collectChangesets(stateRoot, changesets);
}
const mapChangesets = (stateRoot, changesets) => {
  return changesets.entries.map((id: string) => {
    return stateRoot.byId[id];
  });
};

const collectChangesets = memoizeOne(mapChangesets);

export function getChangeset(state: object, repository: Repository, id: string) {
  const key = createItemId(repository);
  const changesets = state.changesets && state.changesets[key] ? state.changesets[key].byId : null;
  if (changesets != null && changesets[id]) {
    return changesets[id];
  }
  return null;
}

export function shouldFetchChangeset(state: object, repository: Repository, id: string) {
  if (getChangeset(state, repository, id)) {
    return false;
  }
  return true;
}

export function isFetchChangesetPending(state: object, repository: Repository, id: string) {
  return isPending(state, FETCH_CHANGESET, createChangesetItemId(repository, id));
}

export function getFetchChangesetFailure(state: object, repository: Repository, id: string) {
  return getFailure(state, FETCH_CHANGESET, createChangesetItemId(repository, id));
}

export function isFetchChangesetsPending(state: object, repository: Repository, branch?: Branch) {
  return isPending(state, FETCH_CHANGESETS, createItemId(repository, branch));
}

export function getFetchChangesetsFailure(state: object, repository: Repository, branch?: Branch) {
  return getFailure(state, FETCH_CHANGESETS, createItemId(repository, branch));
}

const EMPTY = {};

const selectList = (state: object, repository: Repository, branch?: Branch) => {
  const repoId = createItemId(repository);

  const branchName = branch ? branch.name : "";
  if (state.changesets[repoId]) {
    const repoState = state.changesets[repoId];

    if (repoState.byBranch && repoState.byBranch[branchName]) {
      return repoState.byBranch[branchName];
    }
  }
  return EMPTY;
};

const selectListEntry = (state: object, repository: Repository, branch?: Branch): object => {
  const list = selectList(state, repository, branch);
  if (list.entry) {
    return list.entry;
  }
  return EMPTY;
};

export const selectListAsCollection = (state: object, repository: Repository, branch?: Branch): PagedCollection => {
  return selectListEntry(state, repository, branch);
};
