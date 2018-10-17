// @flow

import {
  FAILURE_SUFFIX,
  PENDING_SUFFIX,
  SUCCESS_SUFFIX
} from "../../modules/types";
import { apiClient } from "@scm-manager/ui-components";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";
import { combineReducers } from "redux";
import type {
  Action,
  Branch,
  Changeset,
  PagedCollection,
  Repository
} from "@scm-manager/ui-types";

export const FETCH_CHANGESETS = "scm/repos/FETCH_CHANGESETS";
export const FETCH_CHANGESETS_PENDING = `${FETCH_CHANGESETS}_${PENDING_SUFFIX}`;
export const FETCH_CHANGESETS_SUCCESS = `${FETCH_CHANGESETS}_${SUCCESS_SUFFIX}`;
export const FETCH_CHANGESETS_FAILURE = `${FETCH_CHANGESETS}_${FAILURE_SUFFIX}`;

//TODO: Content type
// actions

export function fetchChangesets(
  repository: Repository,
  branch?: Branch,
  page: number
) {
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

function createChangesetsLink(
  repository: Repository,
  branch?: Branch,
  page: number
) {
  let link = repository._links.changesets.href;

  if (branch) {
    link = branch._links.history.href;
  }

  if (page) {
    link = link + `?page=${page - 1}`;
  }
  return link;
}

export function fetchChangesetsPending(
  repository: Repository,
  branch?: Branch
): Action {
  const itemId = createItemId(repository, branch);

  return {
    type: FETCH_CHANGESETS_PENDING,
    itemId
  };
}

export function fetchChangesetsSuccess(
  repository: Repository,
  branch?: Branch,
  changesets: any
): Action {
  return {
    type: FETCH_CHANGESETS_SUCCESS,
    payload: changesets,
    itemId: createItemId(repository, branch)
  };
}

function fetchChangesetsFailure(
  repository: Repository,
  branch?: Branch,
  error: Error
): Action {
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
  action: Action = { type: "UNKNOWN" }
): Object {
  switch (action.type) {
    case FETCH_CHANGESETS_SUCCESS:
      const changesets = action.payload._embedded.changesets;
      const changesetIds = changesets.map(c => c.id);
      const key = action.itemId;

      if (!key) {
        return state;
      }

      let oldChangesets = { [key]: {} };
      if (state[key]) {
        oldChangesets[key] = state[key];
      }
      const byIds = extractChangesetsByIds(changesets);
      return {
        ...state,
        [key]: {
          byId: byIds,
          list: {
            entries: changesetIds,
            entry: {
              page: action.payload.page,
              pageTotal: action.payload.pageTotal,
              _links: action.payload._links
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

  for (let changeset of changesets) {
    changesetsByIds[changeset.id] = changeset;
  }

  return changesetsByIds;
}

//selectors
export function getChangesets(
  state: Object,
  repository: Repository,
  branch?: Branch
) {
  const key = createItemId(repository, branch);
  if (!state.changesets[key]) {
    return null;
  }
  return Object.values(state.changesets[key].byId);
}

export function isFetchChangesetsPending(
  state: Object,
  repository: Repository,
  branch?: Branch
) {
  return isPending(state, FETCH_CHANGESETS, createItemId(repository, branch));
}

export function getFetchChangesetsFailure(
  state: Object,
  repository: Repository,
  branch?: Branch
) {
  return getFailure(state, FETCH_CHANGESETS, createItemId(repository, branch));
}

const selectList = (state: Object, repository: Repository, branch?: Branch) => {
  const itemId = createItemId(repository, branch);
  if (state.changesets[itemId] && state.changesets[itemId].list) {
    return state.changesets[itemId].list;
  }
  return {};
};

const selectListEntry = (
  state: Object,
  repository: Repository,
  branch?: Branch
): Object => {
  const list = selectList(state, repository, branch);
  if (list.entry) {
    return list.entry;
  }
  return {};
};

export const selectListAsCollection = (
  state: Object,
  repository: Repository,
  branch?: Branch
): PagedCollection => {
  return selectListEntry(state, repository, branch);
};
