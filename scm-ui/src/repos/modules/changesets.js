// @flow

import {FAILURE_SUFFIX, PENDING_SUFFIX, SUCCESS_SUFFIX} from "../../modules/types";
import {apiClient} from "@scm-manager/ui-components";
import {isPending} from "../../modules/pending";
import {getFailure} from "../../modules/failure";
import {combineReducers} from "redux";
import type {Action, Changeset, PagedCollection, Repository} from "@scm-manager/ui-types";

export const FETCH_CHANGESETS = "scm/repos/FETCH_CHANGESETS";
export const FETCH_CHANGESETS_PENDING = `${FETCH_CHANGESETS}_${PENDING_SUFFIX}`;
export const FETCH_CHANGESETS_SUCCESS = `${FETCH_CHANGESETS}_${SUCCESS_SUFFIX}`;
export const FETCH_CHANGESETS_FAILURE = `${FETCH_CHANGESETS}_${FAILURE_SUFFIX}`;

const REPO_URL = "repositories";
//TODO: Content type
// actions

export function fetchChangesetsByLink(
  repository: Repository,
  link: string,
  branch?: Branch
) {
  return function(dispatch: any) {
    dispatch(fetchChangesetsPending(repository, branch));
    return apiClient
      .get(link)
      .then(response => response.json())
      .then(data => {
        dispatch(fetchChangesetsSuccess(data, repository, branch));
      })
      .catch(cause => {
        dispatch(fetchChangesetsFailure(repository, cause, branch));
      });
  };
}

export function fetchChangesetsWithOptions(
  repository: Repository,
  branch?: Branch,
  suffix?: string
) {
  let link = repository._links.changesets.href;

  if (branch) {
    link = branch._links.history.href;
  }

  if (suffix) {
    link = link + `${suffix}`;
  }

  return function(dispatch: any) {
    dispatch(fetchChangesetsPending(repository, branch));
    return apiClient
      .get(link)
      .then(response => response.json())
      .then(data => {
        dispatch(fetchChangesetsSuccess(data, repository, branch));
      })
      .catch(cause => {
        dispatch(fetchChangesetsFailure(repository, cause, branch));
      });
  };
}

export function fetchChangesets(repository: Repository) {
  return fetchChangesetsWithOptions(repository);
}

export function fetchChangesetsByPage(repository: Repository, page: number) {
  return fetchChangesetsWithOptions(repository, "", `?page=${page - 1}`);
}

// TODO: Rewrite code to fetch changesets by branches, adjust tests and let BranchChooser fetch branches
export function fetchChangesetsByBranchAndPage(
  repository: Repository,
  branch: Branch,
  page: number
) {
  return fetchChangesetsWithOptions(repository, branch, `?page=${page - 1}`);
}

export function fetchChangesetsByBranch(
  repository: Repository,
  branch: Branch
) {
  return fetchChangesetsWithOptions(repository, branch);
}

export function fetchChangesetsPending(
  repository: Repository,
  branch?: Branch
): Action {
  const itemId = createItemId(repository, branch);
  if (!branch) {
    branch = "";
  }
  return {
    type: FETCH_CHANGESETS_PENDING,
    payload: { repository, branch },
    itemId
  };
}

export function fetchChangesetsSuccess(
  changesets: any,
  repository: Repository,
  branch?: Branch
): Action {
  return {
    type: FETCH_CHANGESETS_SUCCESS,
    payload: changesets,
    itemId: createItemId(repository, branch)
  };
}

function fetchChangesetsFailure(
  repository: Repository,
  error: Error,
  branch?: Branch
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
  if (branch && branch !== "") {
    itemId = itemId + "/" + branch.name;
  }
  return itemId;
}

// reducer
function byKeyReducer(
  state: any = {},
  action: Action = { type: "UNKNOWN" }
): Object {
  switch (action.type) {
    case FETCH_CHANGESETS_SUCCESS:
      const changesets = action.payload._embedded.changesets;
      const changesetIds = changesets.map(c => c.id);
      const key = action.itemId;
      let oldChangesets = { [key]: {} };
      if (state[key] !== undefined) {
        oldChangesets[key] = state[key];
      }
      const byIds = extractChangesetsByIds(changesets, oldChangesets[key].byId);
      return {
        ...state,
        [key]: {
          byId: { ...byIds },
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

export default combineReducers({
  byKey: byKeyReducer
});

function extractChangesetsByIds(changesets: any, oldChangesetsByIds: any) {
  const changesetsByIds = {};

  for (let changeset of changesets) {
    changesetsByIds[changeset.id] = changeset;
  }

  for (let id in oldChangesetsByIds) {
    changesetsByIds[id] = oldChangesetsByIds[id];
  }

  return changesetsByIds;
}

//selectors
export function getChangesets(
  state: Object,
  repository: Repository,
  branch?: string
) {
  const key = createItemId(repository, branch);
  if (!state.changesets.byKey[key]) {
    return null;
  }
  return Object.values(state.changesets.byKey[key].byId);
}

export function isFetchChangesetsPending(
  state: Object,
  repository: Repository,
  branch?: string
) {
  return isPending(state, FETCH_CHANGESETS, createItemId(repository, branch));
}

export function getFetchChangesetsFailure(
  state: Object,
  repository: Repository,
  branch?: string
) {
  return getFailure(state, FETCH_CHANGESETS, createItemId(repository, branch));
}

const selectList = (state: Object, key: string) => {
  if (state.changesets.byKey[key] && state.changesets.byKey[key].list) {
    return state.changesets.byKey[key].list;
  }
  return {};
};

const selectListEntry = (state: Object, key: string): Object => {
  const list = selectList(state, key);
  if (list.entry) {
    return list.entry;
  }
  return {};
};

export const selectListAsCollection = (
  state: Object,
  key: string
): PagedCollection => {
  return selectListEntry(state, key);
};

export function getChangesetsFromState(state: Object, key: string) {
  const changesetIds = selectList(state, key).entries;
  if (!changesetIds) {
    return null;
  }
  const changesetEntries: Changeset[] = [];

  for (let id of changesetIds) {
    changesetEntries.push(state.changesets.byKey[key].byId[id]);
  }

  return changesetEntries;
}
